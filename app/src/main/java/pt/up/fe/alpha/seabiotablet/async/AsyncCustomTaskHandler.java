package pt.up.fe.alpha.seabiotablet.async;

import pt.up.fe.alpha.seabiotablet.models.ProgressUpdateItem;

/**
 * @param <T> Object type
 */
public interface AsyncCustomTaskHandler<T> {

    void onSuccess(T result);

    void onFailure(Exception error);

    void onProgressUpdate(ProgressUpdateItem progress);

}
