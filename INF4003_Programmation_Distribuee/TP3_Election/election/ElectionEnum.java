package election;

enum ElectionEnum {

    INITIAL,
    CANDIDATE,
    DEFEATED,
    ELECTED;

    static final String EXPLORE_TAG = "election_explorer_tag";
    static final String TURN_BACK_TAG = "election_turn_back_tag";
    static final String CONCLUDE_TAG = "election_conclude_tag";
}