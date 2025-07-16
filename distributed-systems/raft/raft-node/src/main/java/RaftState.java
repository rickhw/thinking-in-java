public class RaftState {
    public int currentTerm = 0;
    public String votedFor = null;
    public Role role = Role.FOLLOWER;
    public int commitIndex = 0;
    public int lastApplied = 0;

    public synchronized void becomeFollower(int term) {
        this.currentTerm = term;
        this.role = Role.FOLLOWER;
        this.votedFor = null;
    }
}
