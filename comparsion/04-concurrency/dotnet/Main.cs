using System.Net.Http;
using System.Threading.Tasks;

public class Main {
    public static async Task Main() {
        using HttpClient client = new HttpClient();
        Task[] tasks = new Task[1000];
        for (int i = 0; i < 1000; i++) {
            tasks[i] = client.GetStringAsync("http://example.com");
        }
        await Task.WhenAll(tasks);
    }
}