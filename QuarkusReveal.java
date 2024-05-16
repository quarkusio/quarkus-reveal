//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.6.3
//DEPS io.quarkus.platform:quarkus-bom:3.10.0@pom
//DEPS io.quarkus:quarkus-rest
//DEPS io.quarkiverse.web-bundler:quarkus-web-bundler:1.5.0.CR1
//DEPS org.mvnpm:reveal.js:5.1.0
//JAVAC_OPTIONS -parameters
//JAVA_OPTIONS -Djava.util.logging.manager=org.jboss.logmanager.LogManager
//FILES web/index.html=web/index.html
//FILES web/app/github.css=web/app/github.css
//FILES web/app/main.css=web/app/main.css
//FILES web/app/main.js=web/app/main.js
//Q:CONFIG quarkus.web-bundler.dependencies.compile-only=false
//Q:CONFIG quarkus.http.port=7979

import io.quarkus.runtime.Quarkus;
import io.vertx.core.http.impl.MimeMapping;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;



@CommandLine.Command(name = "quarkus-reveal", mixinStandardHelpOptions = true, version = "0.1",
        description = "Develop and use your Reveal.js decks easily with Quarkus")
public class QuarkusReveal implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The greeting to print", defaultValue = "./demo.md")
    private String deck;

    public static void main(String... args) {
        int exitCode = new CommandLine(new QuarkusReveal()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        System.setProperty("deck", deck);
        Quarkus.run();
        return 0;
    }

    @ApplicationScoped
    @Path("/")
    public static class RestResource {

        @ConfigProperty(name = "deck")
        String deck;

        @GET
        @Path("demo.md")
        @Produces("text/markdown")
        public String getDeck() throws IOException {
            final java.nio.file.Path demo = java.nio.file.Path.of(deck);
            if(!Files.exists(demo)) {
                throw new IOException("Deck file not found: " + demo);
            }
            return Files.readString(demo);
        }

        @GET
        @Path("assets/{name}")
        public Response getAsset(@PathParam("name") String name) throws IOException {
            final java.nio.file.Path deckPath = java.nio.file.Path.of(deck).toAbsolutePath();
            final java.nio.file.Path assets = deckPath.getParent().resolve("assets");
            byte[] asset = findAsset(name, assets);
            if (asset == null)
                asset = findAsset(name, deckPath.getParent().getParent().resolve("assets"));
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