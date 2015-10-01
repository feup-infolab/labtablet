package pt.up.fe.labtablet.async;

import pt.up.fe.labtablet.models.ProgressUpdateItem;

/**
 * @param <T> Object type
 */
public interface AsyncCustomTaskHandler<T> {

    void onSuccess(T result);

    void onFailure(Exception error);

    void onProgressUpdate(ProgressUpdateItem progress);

}
