package pt.up.fe.labtablet.api;

/**
 * Interface for each of the fragments communicate with the
 * parent activity in the submission stages
 */
public interface SubmissionStepHandler {
    void nextStep(int stage);
}