//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.6.3
//DEPS io.quarkus.platform:quarkus-bom:3.11.0.CR1@pom
//DEPS io.quarkus:quarkus-rest
//DEPS io.quarkiverse.web-bundler:quarkus-web-bundler:1.5.0.CR1
//DEPS io.quarkiverse.qute.web:quarkus-qute-web
//DEPS org.mvnpm:reveal.js:5.1.0
//JAVAC_OPTIONS -parameters
//JAVA_OPTIONS -Djava.util.logging.manager=org.jboss.logmanager.LogManager
//FILES templates/=templates/**/*
//FILES web/=web/**/*
//FILES demo.md

//Q:CONFIG quarkus.web-bundler.bundle.app=true
//Q:CONFIG quarkus.web-bundler.bundle.theme-default=true
//Q:CONFIG quarkus.web-bundler.bundle.theme-quarkus=true
//Q:CONFIG quarkus.web-bundler.bundle.theme-light=true
//Q:CONFIG quarkus.qute.suffixes=html,js
//Q:CONFIG quarkus.web-bundler.bundling.external=/config.js
//Q:CONFIG quarkus.web-bundler.dependencies.compile-only=false
//Q:CONFIG quarkus.http.port=7979
package ia3andy;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestPath;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Quarkus;
import io.vertx.core.http.impl.MimeMapping;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import picocli.CommandLine;

@CommandLine.Command(name = "quarkus-reveal", mixinStandardHelpOptions = true, version = "0.1", description = "Develop and use your Reveal.js decks easily with Quarkus")
public class QuarkusReveal implements Callable<Integer> {
    private static final Pattern FRONTMATTER_PATTERN = Pattern.compile("^---\\n.*?\\n---", Pattern.DOTALL);
    private static final FileCache FILE_CACHE = new FileCache();

    @CommandLine.Parameters(index = "0", description = "The slides deck", defaultValue = "deck.md")
    private String deck;

    @CommandLine.Option(names = { "-t", "--theme" }, description = "The theme to use", defaultValue = "default")
    private String theme;

    @CommandLine.Option(names = { "--title" }, description = "The title to use", defaultValue = "Demo")
    private String title;

    @CommandLine.Option(names = { "-p", "--port" }, description = "The http port", defaultValue = "7979")
    private String port;

    public static void main(String... args) {
        int exitCode = new CommandLine(new QuarkusReveal()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        String resolvedDeck = deck;
        final Path deckPath = Path.of(deck);
        if (!Files.exists(deckPath)) {
            if (Objects.equals(deck, "deck.md")) {
                // Let's use the demo deck
                resolvedDeck = "DEMO";
            } else {
                throw new IOException("Deck file not found: " + deck);
            }
        }
        // Only read if not using the demo deck
        if (!resolvedDeck.equals("DEMO")) {
        var content = FILE_CACHE.read(deckPath).contentAsString();
        if (hasFrontMatter(content)) {
            final var fm = parseFrontMatter(content);
            if (fm.containsKey("theme") && theme.equals("default")) {
                theme = fm.get("theme");
            }
            if (fm.containsKey("title") && title.equals("Demo")) {
                title = fm.get("title");
            }
            if (fm.containsKey("port") && port.equals("7979")) {
                port = fm.get("port");
            }
            setFromFM(fm, "width");
            setFromFM(fm, "height");
            setFromFM(fm, "margin");
        }
    }
        System.setProperty("deck", resolvedDeck);
        System.setProperty("title", title);
        System.setProperty("theme", "theme-" + theme);
        System.setProperty("quarkus.http.port", port);
        System.out.println("Starting with deck: " + resolvedDeck + " and theme: " + theme);
        Quarkus.run();
        return 0;
    }

    private static void setFromFM(Map<String, String> fm, String key) {
        if (fm.containsKey(key)) {
            System.setProperty(key, fm.get(key));
        }
    }

    @Named("theme")
    @Singleton
    public static class ThemeSelector {

        @ConfigProperty(name = "theme")
        String theme;

        public boolean isCustom() {
            return !isBuiltin();
        }

        public boolean isBuiltin() {
            return switch (theme) {
                case "theme-default", "theme-light", "theme-quarkus" -> true;
                default -> false;
            };
        }

        public String name() {
            return theme;
        }

    }

    @Singleton
    @jakarta.ws.rs.Path("/")
    public static class RestResource {

        @ConfigProperty(name = "deck")
        String deck;

        @GET
        @jakarta.ws.rs.Path("deck.md")
        @Produces("text/markdown")
        public String getDeck() throws IOException {
            if (deck.equals("DEMO")) {
                try (InputStream demoStream = RestResource.class.getResourceAsStream("/demo.md")) {
                    if (demoStream == null) {
                        throw new IOException("Demo deck not found");
                    }
                    return processContent(new String(demoStream.readAllBytes(), Charset.defaultCharset()));
                }
            } else {
                final java.nio.file.Path deckFile = java.nio.file.Path.of(deck);
                final FileCache.Result result = FILE_CACHE.read(deckFile);
                return processContent(result.contentAsString());
            }

        }

        @GET
        @jakarta.ws.rs.Path("deck-assets/{name}")
        public Response getAsset(@PathParam("name") String name) throws IOException {
            final java.nio.file.Path deckPath = java.nio.file.Path.of(deck).toAbsolutePath();
            final java.nio.file.Path assets = deckPath.getParent().resolve("deck-assets");
            Path asset = findAsset(name, assets);
            if (asset == null)
                asset = findAsset(name, deckPath.getParent().getParent().resolve("deck-assets"));
            if (asset == null) {
                throw new IOException("Deck asset not found: " + name);
            }
            return Response.ok()
                    .entity(FILE_CACHE.read(asset).content())
                    .type(MimeMapping.getMimeTypeForFilename(name))
                    .build();
        }

        @GET
        @jakarta.ws.rs.Path("theme/{name}/{assetPath: .*}")
        public Response getThemeAsset(@RestPath String name, @RestPath String assetPath) throws IOException {
            Log.debugf("Get %s asset: %s", name, assetPath);
            Path deckPath = Path.of(deck).toAbsolutePath();
            Path themePath = deckPath.getParent().resolve(name);
            if (!Files.exists(themePath)) {
                // Also try the parent dir
                themePath = deckPath.getParent().getParent().resolve(name);
                if (!Files.exists(themePath)) {
                    return Response.status(Status.NOT_FOUND).build();
                }
            }
            Path asset = findAsset(assetPath, themePath);
            if (asset == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok()
                    .entity(FILE_CACHE.read(asset).content())
                    .type(MimeMapping.getMimeTypeForFilename(name))
                    .build();
        }

        private static Path findAsset(String name, java.nio.file.Path assets) throws IOException {
            if (!Files.isDirectory(assets)) {
                return null;
            }
            final java.nio.file.Path asset = assets.resolve(name);
            if (!Files.isRegularFile(asset)) {
                return null;
            }
            return asset;
        }
    }

    public static class FileCache {

        private final Map<Path, CachedFile> cache = new ConcurrentHashMap<>();

        public Result read(final Path path) throws IOException {
            if (!Files.exists(path)) {
                throw new IOException("File not found: " + path);
            }
            var lastModified = Files.getLastModifiedTime(path).toMillis();
            var changed = new AtomicBoolean(false);
            var cached = cache.compute(path, (k, v) -> {
                if (v == null || lastModified != v.lastModified()) {
                    try {
                        final byte[] content = Files.readAllBytes(path);
                        changed.set(true);
                        return new CachedFile(content, lastModified);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }

                }
                return v;
            });

            return new Result(cached.content(), changed.get());
        }

        record CachedFile(byte[] content, long lastModified) {
        }

        record Result(byte[] content, boolean changed) {

            public String contentAsString() {
                return new String(content(), StandardCharsets.UTF_8);
            }
        }

    }

    private static Map<String, String> parseFrontMatter(String content) {
        Map<String, String> frontMatter = new HashMap<>();

        if (hasFrontMatter(content)) {
            int endOfFrontMatter = content.indexOf("---", 3);
            if (endOfFrontMatter != -1) {
                String frontMatterContent = content.substring(3, endOfFrontMatter).trim();

                Pattern pattern = Pattern.compile("^(\\w+):\\s*(.*)$", Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(frontMatterContent);
                while (matcher.find()) {
                    frontMatter.put(matcher.group(1), matcher.group(2).trim());
                }
            }
        }

        return frontMatter;
    }

    private static String processContent(String content) {
        return replaceFragments(stripFrontMatter(content));
    }

    private static String replaceFragments(String content) {
        return content.replaceAll("\\[~([a-z- ]+)\\]", "&shy;<!-- .element: class=\"fragment $1\" -->")
                .replaceAll("\\[~\\]", "&shy;<!-- .element: class=\"fragment\" -->");
    }

    private static String stripFrontMatter(String content) {
        if (hasFrontMatter(content)) {
            return FRONTMATTER_PATTERN.matcher(content).replaceAll("");
        }
        return content;
    }

    private static boolean hasFrontMatter(String content) {
        return FRONTMATTER_PATTERN.matcher(content).find();
    }

}
