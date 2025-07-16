public class AppendEntriesRequest {
    public int term;

    public AppendEntriesRequest() {}

    public AppendEntriesRequest(int term) {
        this.term = term;
    }
}
