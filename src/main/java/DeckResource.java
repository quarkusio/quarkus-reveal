import io.vertx.core.http.impl.MimeMapping;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;

@Path("/")
public class DeckResource {

    @ConfigProperty(name = "deck", defaultValue = "./demo.md")
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
        final java.nio.file.Path deckPath = java.nio.file.Path.of(deck);
        final java.nio.file.Path assets = deckPath.getParent().resolve("assets");
        if(!Files.isDirectory(assets)) {
            throw new IOException("Deck assets not found: " + assets);
        }
        final java.nio.file.Path asset = assets.resolve(name);
        if(!Files.isRegularFile(asset)) {
            throw new IOException("Deck asset not found: " + asset);
        }
        return Response.ok()
                .entity(Files.readAllBytes(asset))
                .type(MimeMapping.getMimeTypeForFilename(name))
                .build();
    }
}
