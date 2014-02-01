package turkycat.android.raindots.drawables;

import java.util.Random;

import turkycat.android.raindots.views.GameView.Mode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class DrawableCircle
{
	private static final String TAG = "DrawableCircle";
	
	private boolean enabled;
	
	public float x;
	public float y;
	
	private int maxX;
	private int maxY;
	
	private float time;
	private float speed;
	
	private Paint paint;
	
	public float radius;
	public int color;
	private Random rnd = new Random();
	
	
	//---------------------------------------------------------------------------circle constructors
	public DrawableCircle( float x, float y )
	{
		init( x, y, Integer.MAX_VALUE, Integer.MAX_VALUE, false );
	}
	
	public DrawableCircle( float x, float y, int maxX, int maxY )
	{
		init( x, y, maxX, maxY, false );
	}
	
	public DrawableCircle( float x, float y, int maxX, int maxY, boolean setColors )
	{
		init( x, y, maxX, maxY, setColors );
	}
	
	/**
	 * initializes a DrawableCircle object
	 */
	private void init( float x, float y, int maxX, int maxY, boolean setColors )
	{
		this.enabled = true;
		this.time = 0.0f;
		this.speed = 0.0f;
		this.x = x;
		this.y = y;
		this.radius = rnd.nextInt( 150 ) + 1.0f;
		
		if( setColors )
		{
			switch (rnd.nextInt(7))
			{
			case 0:
				color = Color.RED;
				break;

			case 1:
				color = Color.BLUE;
				break;

			case 2:
				color = Color.CYAN;
				break;

			case 3:
				color = Color.GREEN;
				break;

			case 4:
				color = Color.MAGENTA;
				break;

			case 5:
				color = Color.WHITE;
				break;

			case 6:
				color = Color.YELLOW;
				break;
			}
		}
		else
		{
//			color = Color.argb( rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256) );
			color = Color.rgb( rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256) );
		}

		this.maxX = maxX;
		this.maxY = maxY;
	}
	
	
	
	//--------------------------------------------------------------------------------------public functions
	
	
	/*
	 * asks the circle to draw itself.
	 */
	public void draw( Canvas canvas )
	{
		Paint paint = new Paint();
		
		paint.setColor( Color.WHITE );
		canvas.drawCircle( x, y, radius + 2f, paint );
		
		paint.setColor( color );
		canvas.drawCircle( x, y, radius, paint );
		Log.i( TAG, "circle drawn" );
	}
	
	
	public boolean update( Mode mode )
	{
		if( !enabled ) return false;

		time++;
		switch( mode )
		{
		default:
		case GRAVITY:
		case DENSITY:
			
			if( y >= maxY - radius )
			{
				y += 2.0f;
				radius -= 1.0f;
				if( radius <= 0.0f ) enabled = false;
			}
			else
			{
				
				if( mode == Mode.DENSITY )
				{
					speed += ( 0.2f * time ) / radius;
				}
				else if( mode == Mode.GRAVITY )
				{
					speed += ( 0.02f * Math.sqrt( time ) * Math.sqrt( radius ) );
				}
				
				this.y += speed;
			}

			break;
			
			//antigravity can move things up or down
		case ANTIGRAVITY:
			if( isOutOfBounds() ) enabled = false;
			else
			{
				speed -= ( rnd.nextFloat() - 0.5f ) * 2;
				y += speed;
				radius += ( rnd.nextFloat() - 0.5f ) * 5;
				x += rnd.nextInt(2) == 0 ? -1 : 1;
			}
			
			break;

		case EVAPORATE:
			
			this.radius--;
			if( radius <= 0 ) enabled = false;
			
			break;
			
		case SEIZURE:
			color = Color.rgb( rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256) );
			this.radius = rnd.nextFloat() * 200.0f;
			
			switch( rnd.nextInt( 5 ) )
			{
			case 0:
				if( y >= maxY - radius )
				{
					y += 2.0f;
					radius -= 1.0f;
					if( radius <= 0.0f ) enabled = false;
				}
				
				break;
				
			case 1:
				y -= rnd.nextInt( 10 );
				x -= rnd.nextInt( 10 );
				
				break;
				
			case 2:
				y -= rnd.nextInt( 10 );
				x += rnd.nextInt( 10 );
				
				break;
				
			case 3:
				speed -= rnd.nextFloat() * 2.0f;
				
				break;
				
			case 4:
				speed += rnd.nextFloat() * 2.0f;
				
				break;
			}
			
			speed += ( 0.2f * time * radius ) / ( radius * radius );
			this.y += speed;
			
			break;
		}
		return enabled;
	}
	
	
	
	private boolean isOutOfBounds()
	{
		//too large?
		if( y > ( maxY + radius ) || x > ( maxX + radius )  ) return true;
		//too small?
		if( y < -radius || x < -radius ) return true;
		
		//we're good.
		return false;
	}
}
