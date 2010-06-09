package brown.android.tictac;

import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import brown.games.Evaluation;
import brown.games.GameMove;
import brown.games.algos.MiniMaxEvaluation;
import brown.games.tictac.TicTacGameMove;
import brown.games.tictac.TicTacGameState;
import brown.games.tictac.TicTacPlayer;
import brown.games.tictac.Tile;

public class GameActivity extends Activity implements View.OnClickListener {

	/** Log tag */
	private static final String TAG = "brown.GameActicity";

	private int[] imageIds = new int[]{ R.id.tile1, R.id.tile2, R.id.tile3, R.id.tile4, R.id.tile5,
			R.id.tile6, R.id.tile7, R.id.tile8, R.id.tile9 };

	private Map<Tile, Integer> resourceMap;

	private Map<Tile, Tile> nextTileMap;

	private TextView status;

	private Evaluation eval;

	private TicTacPlayer human;

	private TicTacPlayer computer;

	private TicTacGameState state;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		resourceMap = new HashMap<Tile, Integer>();
		resourceMap.put(Tile.X, R.drawable.x);
		resourceMap.put(Tile.O, R.drawable.o);
		resourceMap.put(null, R.drawable.empty);

		nextTileMap = new HashMap<Tile, Tile>();
		nextTileMap.put(null, Tile.X);
		nextTileMap.put(Tile.X, Tile.O);
		nextTileMap.put(Tile.O, null);

		// TODO restorable state
		eval = new MiniMaxEvaluation(2);
		human = new TicTacPlayer(Tile.X);
		computer = new TicTacPlayer(Tile.O);
		state = new TicTacGameState();

		status = (TextView) findViewById(R.id.statusText);

		for (int id : imageIds) {
			findViewById(id).setOnClickListener(this);
		}

		Log.d(TAG, "onCreate: complete");
	}

	@Override
	public void onClick(View v) {
		Log.d(TAG, "onClick: " + v);
		ImageView thisImageView = (ImageView) v;

		// which view am i? find index in array
		int ix = -1;
		for (int i = 0; i < imageIds.length; i++) {
			if (imageIds[i] == v.getId()) {
				ix = i;
				break;
			}
		}

		// clicked on something other than game board
		if (ix == -1) return;

		int row = ix % 3;
		int col = ix / 3;

		GameMove move = new TicTacGameMove(human.getTile(), row, col);
		if (move.isValid(state)) {
			move.execute(state);
			thisImageView.setImageResource(R.drawable.x);
			checkForGameOver();
			
			if (!state.isDraw() && !state.isWin()) {
				Log.d(TAG, "onClick: launching EvaluateMovesTask");
				new EvaluateMovesTask().execute();
			}
		}

		

	}

	private void checkForGameOver() {
		boolean gameOver = false;

		if (state.isDraw()) {
			status.setText(R.string.status_draw);
			gameOver = true;
		}
		else if (state.isWin()) {
			gameOver = true;
			if (state.isWinner(human)) {
				status.setText(R.string.status_won);
			}
			else {
				status.setText(R.string.status_lost);
			}
		}

		// remove onclick listeners
		if (gameOver) {
			for (int id : imageIds) {
				findViewById(id).setClickable(false);
			}
		}
	}

	/**
	 * Executes evaluation of computer move on a background thread
	 * 
	 * @author Matt Brown msbcode@gmail.com
	 * @date Jun 8, 2010
	 */
	// can i make this static?
	private class EvaluateMovesTask extends AsyncTask<Void, Void, TicTacGameMove> {

		@Override
		protected TicTacGameMove doInBackground(Void... params) {
			TicTacGameMove oppMove = (TicTacGameMove) eval.bestMove(state, computer, human);
			oppMove.execute(state);

			return oppMove;
		}

		@Override
		protected void onPostExecute(TicTacGameMove result) {
			// find tile of this move
			final int id = result.getRow() + result.getColumn() * 3;
			ImageView oppView = (ImageView) findViewById(imageIds[id]);
			oppView.setImageResource(R.drawable.o);

			status.setText(R.string.status_yourmove);

			checkForGameOver();
		}
		
	}

}