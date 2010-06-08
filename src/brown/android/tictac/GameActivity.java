package brown.android.tictac;

import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import brown.games.Evaluation;
import brown.games.GameMove;
import brown.games.algos.MiniMaxEvaluation;
import brown.games.tictac.TicTacGameMove;
import brown.games.tictac.TicTacGameState;
import brown.games.tictac.TicTacPlayer;
import brown.games.tictac.Tile;

public class GameActivity extends Activity {

	private int[] images = new int[]{ R.id.tile1, R.id.tile2, R.id.tile3, R.id.tile4, R.id.tile5,
			R.id.tile6, R.id.tile7, R.id.tile8, R.id.tile9 };

	private Tile[] tileState = new Tile[images.length];

	private Map<Tile, Integer> resourceMap;

	private Map<Tile, Tile> nextTileMap;

	private boolean gameOver = false;

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

		final Evaluation eval = new MiniMaxEvaluation(2);
		final TicTacPlayer human = new TicTacPlayer(Tile.X);
		final TicTacPlayer computer = new TicTacPlayer(Tile.O);
		final TicTacGameState state = new TicTacGameState();

		OnClickListener clickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (gameOver) return;

				ImageView thisImageView = (ImageView) v;
				TextView status = (TextView) findViewById(R.id.statusText);

				// find index in array
				int ix = -1;
				for (int i = 0; i < images.length; i++) {
					if (images[i] == v.getId()) {
						ix = i;
						break;
					}
				}

				GameMove move = new TicTacGameMove(human.getTile(), ix % 3, ix / 3);
				if (move.isValid(state)) {
					move.execute(state);
					thisImageView.setImageResource(R.drawable.x);
					thisImageView.invalidate();

					if (!state.isDraw() && !state.isWin()) {
						TicTacGameMove oppMove = (TicTacGameMove) eval.bestMove(state, computer,
							human);
						oppMove.execute(state);
						// find tile of this move
						final int id = oppMove.getRow() + oppMove.getColumn() * 3;
						ImageView oppView = (ImageView) findViewById(images[id]);
						oppView.setImageResource(R.drawable.o);
						oppView.invalidate();

						status.setText(R.string.status_yourmove);

					}
				}

				checkForGameOver(human, state, status);

			}

			/**
			 * @param human
			 * @param state
			 * @param status
			 */
			private void checkForGameOver(final TicTacPlayer human, final TicTacGameState state,
					TextView status) {

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
			}
		};

		for (int id : images) {
			findViewById(id).setOnClickListener(clickListener);
		}
	}

}