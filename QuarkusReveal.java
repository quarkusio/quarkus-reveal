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
//Q:CONFIG quarkus.web-bundler.dependencies.compile-only=false
//Q:CONFIG quarkus.http.port=7979
package ia3andy;

import io.quarkus.runtime.Quarkus;
import io.vertx.core.http.impl.MimeMapping;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.Callable;



@CommandLine.Command(name = "quarkus-reveal", mixinStandardHelpOptions = true, version = "0.1",
        description = "Develop and use your Reveal.js decks easily with Quarkus")
public class QuarkusReveal implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The greeting to print", defaultValue = "deck.md")
    private String deck;

    @CommandLine.Option(names = {"-t", "--theme"}, description = "The theme to use (default or quarkus)", defaultValue = "default")
    private String theme;

    @CommandLine.Option(names = {"-p", "--port"}, description = "The http port", defaultValue = "7979")
    private String port;

    public static void main(String... args) {
        int exitCode = new CommandLine(new QuarkusReveal()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        String resolvedDeck = deck;
        if(!Files.exists(java.nio.file.Path.of(deck))) {
            if (Objects.equals(deck, "deck.md")) {
                // Let's use the demo deck
                resolvedDeck = "DEMO";
            } else {
                throw new IOException("Deck file not found: " + deck);
            }
        }
        System.setProperty("deck", resolvedDeck);
        System.setProperty("theme", "theme-" + theme);
        System.setProperty("quarkus.http.port", port);
        System.out.println("Starting with deck: " + resolvedDeck + " and theme: " + theme);
        Quarkus.run();
        return 0;
    }

    @ApplicationScoped
    @Named("restResource")
    @Path("/")
    public static class RestResource {

        @ConfigProperty(name = "theme")
        String theme;

        @ConfigProperty(name = "deck")
        String deck;

        public String theme() {
            return theme;
        }

        @GET
        @Path("deck.md")
        @Produces("text/markdown")
        public String getDeck() throws IOException {
            if (deck.equals("DEMO")) {
                try (InputStream demoStream = RestResource.class.getResourceAsStream("/demo.md")) {
                    if (demoStream == null) {
                        throw new IOException("Demo deck not found");
                    }
                    return new String(demoStream.readAllBytes(), Charset.defaultCharset());
                }
            } else {
                final java.nio.file.Path deckFile = java.nio.file.Path.of(deck);
                if(!Files.exists(deckFile)) {
                    throw new IOException("Deck file not found: " + deckFile);
                }
                return Files.readString(deckFile);
            }

        }

        @GET
        @Path("deck-assets/{name}")
        public Response getAsset(@PathParam("name") String name) throws IOException {
            final java.nio.file.Path deckPath = java.nio.file.Path.of(deck).toAbsolutePath();
            final java.nio.file.Path assets = deckPath.getParent().resolve("deck-assets");
            byte[] asset = findAsset(name, assets);
            if (asset == null)
                asset = findAsset(name, deckPath.getParent().getParent().resolve("deck-assets"));
            if (asset == null) {
                throw new IOException("Deck asset not found: " + name);
            }
            return Response.ok()
                    .entity(asset)
                    .type(MimeMapping.getMimeTypeForFilename(name))
                    .build();
        }

        private static byte[] findAsset(String name, java.nio.file.Path assets) throws IOException {
            if(!Files.isDirectory(assets)) {
                return null;
            }
            final java.nio.file.Path asset = assets.resolve(name);
            if(!Files.isRegularFile(asset)) {
                return null;
            }
            return Files.readAllBytes(asset);
        }
    }


}