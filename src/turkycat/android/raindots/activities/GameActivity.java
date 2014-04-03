package turkycat.android.raindots.activities;

import turkycat.android.raindots.R;
import turkycat.android.raindots.application.Raindots;
import turkycat.android.raindots.views.GameView;
import turkycat.android.raindots.views.GameView.Mode;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class GameActivity extends Activity
{
	public static final String TAG = "GameActivity";
	private GameView gameView;
	private Raindots application;
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.game_activity );
		
		this.application = (Raindots) getApplication();
		gameView = (GameView) findViewById( R.id.gameView );
		
		Log.i( TAG, "gameView is " + ( gameView == null ? "null." : gameView.toString() ) );
		
		Toast.makeText( gameView.getContext(), "press anywhere and enjoy", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
        getMenuInflater().inflate( R.menu.game_menu, menu );
        return true;
	}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch( item.getItemId() )
		{
		case R.id.gravity:
			gameView.setMode( Mode.GRAVITY );
			break;
			
		case R.id.density:
			gameView.setMode( Mode.DENSITY );
			break;
			
		case R.id.evap:
			gameView.setMode( Mode.EVAPORATE );
			break;
			
		case R.id.paint:
			gameView.setMode( Mode.PAINT );
			break;
			
		case R.id.seizure:
			gameView.setMode( Mode.SEIZURE );
			break;
			
		case R.id.antigravity:
			gameView.setMode( Mode.ANTIGRAVITY );
			break;
			
		case R.id.toggleDrawable:
			gameView.toggleDrawable();
			break;
		}
		return true;
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
	}
}
