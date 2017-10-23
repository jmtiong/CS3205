package sg.edu.nus.cs3205.subsystem3.nfcapplet;

import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.InvocationCallback;

public class ServerConnector {
    private static Client client = ClientBuilder.newClient();

    public static Future<Users> getUsers(final Consumer<Users> callback) {
        Builder builder = client.target("http://cs3205-4-i.comp.nus.edu.sg/api/team1/user/").request();
        /*
         * if (1 > 0) { final CompletableFuture<User[]> completedFuture =
         * CompletableFuture.<User[]>
         * completedFuture(builder.get(User[].class)); return completedFuture; }
         */
        return builder.async().get(new InvocationCallback<Users>() {

            @Override
            public void completed(Users users) {
                callback.accept(users);
            }

            @Override
            public void failed(Throwable t) {
                t.printStackTrace();
                throw new RuntimeException(t);
            }
        });
    }
}
