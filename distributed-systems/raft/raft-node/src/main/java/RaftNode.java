@Service
public class RaftNode {

    private final RaftState state = new RaftState();
    private final RestTemplate restTemplate = new RestTemplate();
    private final List<String> peers = List.of("http://localhost:8081", "http://localhost:8082");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        startElectionTimer();
    }

    private void startElectionTimer() {
        scheduler.scheduleWithFixedDelay(() -> {
            if (state.role == Role.FOLLOWER) {
                state.role = Role.CANDIDATE;
                startElection();
            }
        }, 5, 5, TimeUnit.SECONDS); // 模擬選舉超時
    }

    private void startElection() {
        state.currentTerm++;
        state.votedFor = "self";
        int votes = 1;

        for (String peer : peers) {
            try {
                ResponseEntity<Boolean> response = restTemplate.postForEntity(peer + "/raft/vote",
                    state.currentTerm, Boolean.class);
                if (Boolean.TRUE.equals(response.getBody())) {
                    votes++;
                }
            } catch (Exception ignored) {}
        }

        if (votes > peers.size() / 2) {
            state.role = Role.LEADER;
            sendHeartbeats();
        } else {
            state.becomeFollower(state.currentTerm);
        }
    }

    private void sendHeartbeats() {
        scheduler.scheduleWithFixedDelay(() -> {
            if (state.role != Role.LEADER) return;
            for (String peer : peers) {
                try {
                    restTemplate.postForEntity(peer + "/raft/append", new AppendEntriesRequest(state.currentTerm), Void.class);
                } catch (Exception ignored) {}
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    public boolean requestVote(int term) {
        if (term > state.currentTerm) {
            state.becomeFollower(term);
            state.votedFor = "other";
            return true;
        }
        return false;
    }

    public void receiveAppendEntries(AppendEntriesRequest req) {
        if (req.term >= state.currentTerm) {
            state.becomeFollower(req.term);
        }
    }
    @Autowired
    private MeterRegistry meterRegistry;

    private Counter electionCount;
    private Counter voteGranted;
    private AtomicInteger currentTermGauge;
    private AtomicInteger currentRoleGauge;

    @PostConstruct
    public void initMetrics() {
        electionCount = Counter.builder("raft_election_count_total")
            .description("Total number of elections initiated")
            .tag("node", nodeId)
            .register(meterRegistry);

        voteGranted = Counter.builder("raft_vote_granted_total")
            .description("Number of times this node granted a vote")
            .tag("node", nodeId)
            .register(meterRegistry);

        currentTermGauge = meterRegistry.gauge("raft_term_current", Tags.of("node", nodeId), new AtomicInteger(state.currentTerm));
        currentRoleGauge = meterRegistry.gauge("raft_node_role", Tags.of("node", nodeId), new AtomicInteger(state.role.ordinal()));
    }

}
