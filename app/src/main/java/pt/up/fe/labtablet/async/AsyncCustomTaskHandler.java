package pt.up.fe.labtablet.async;

import pt.up.fe.labtablet.models.ProgressUpdateItem;

/**
 * @param <T> Object type
 */
public interface AsyncCustomTaskHandler<T> {

    public void onSuccess(T result);

    public void onFailure(Exception error);

    public void onProgressUpdate(ProgressUpdateItem progress);

}
