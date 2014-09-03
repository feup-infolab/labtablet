package pt.up.fe.labtablet.api;

import android.os.AsyncTask;

import java.util.ArrayList;

import pt.up.fe.labtablet.models.FavoriteItem;


public class AsyncSearchTask extends AsyncTask<String, Integer, ArrayList<FavoriteItem>> {

	private AsyncTaskHandler<ArrayList<FavoriteItem>> mHandler;
	private Exception error;
	
	public AsyncSearchTask(AsyncTaskHandler<ArrayList<FavoriteItem>> mHandler) {
		this.mHandler = mHandler;
	}
	
	
	@Override
	protected ArrayList<FavoriteItem> doInBackground(String... params) {
		try {
			CkanAPI.Connect();
			return CkanAPI.Search(params[0]);
		} catch (Exception e) {
			error = e;
		}
		return null;
	}


	
	@Override
	protected void onPostExecute(ArrayList<FavoriteItem> result) {
		super.onPostExecute(result);
		if(error != null) {
			mHandler.onFailure(error);
		} else {
			mHandler.onSuccess(result);
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		mHandler.onProgressUpdate(values[0]);
	}
}
