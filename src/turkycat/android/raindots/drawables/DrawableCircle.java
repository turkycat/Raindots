package turkycat.android.raindots.drawables;

import java.util.Random;

import turkycat.android.raindots.views.GameView.Mode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class DrawableCircle extends DrawableItem
{
	private static final String TAG = "DrawableCircle";
	
	
	//---------------------------------------------------------------------------circle constructors
	public DrawableCircle( float x, float y )
	{
		this( x, y, Integer.MAX_VALUE, Integer.MAX_VALUE, false );
	}
	
	public DrawableCircle( float x, float y, int maxX, int maxY )
	{
		this( x, y, maxX, maxY, false );
	}
	
	public DrawableCircle( float x, float y, int maxX, int maxY, boolean setColors )
	{
		init( x, y, maxX, maxY, setColors );
		this.size = rnd.nextInt( 150 ) + 1.0f;
	}
	
	
	
	
	
	//--------------------------------------------------------------------------------------public functions
	
	
	/*
	 * asks the circle to draw itself.
	 */
	public void draw( Canvas canvas )
	{
		Paint paint = new Paint();
		
		paint.setColor( color );
		canvas.drawCircle( x, y, size, paint );
		//Log.i( TAG, "circle drawn" );
	}
	
	

	@Override
	protected boolean isOutOfBounds()
	{
		//too large?
		if( y > ( maxY + size ) || x > ( maxX + size )  ) return true;
		//too small?
		if( y < -size || x < -size ) return true;
		
		//we're good.
		return false;
	}
}
