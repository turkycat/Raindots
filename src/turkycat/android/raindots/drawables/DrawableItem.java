package turkycat.android.raindots.drawables;

import java.util.Random;

import turkycat.android.raindots.views.GameView.Mode;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public abstract class DrawableItem
{
	protected boolean enabled;
	
	public float x;
	public float y;
	
	protected int maxX;
	protected int maxY;
	
	protected float time;
	protected float speed;
	public float size;
	
	protected Paint paint;
	
	public int color;
	protected Random rnd = new Random();
	
	
	/**
	 * initializes a DrawableCircle object
	 */
	protected void init( float x, float y, int maxX, int maxY, boolean setColors )
	{
		this.enabled = true;
		this.time = 0.0f;
		this.speed = 0.0f;
		this.x = x;
		this.y = y;
		
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

	public abstract void draw( Canvas canvas );
	//public abstract boolean update( Mode mode );
	
	public boolean update( Mode mode )
	{
		if( !enabled ) return false;

		time++;
		switch( mode )
		{
		default:
		case GRAVITY:
		case DENSITY:
			
			if( y >= maxY - size )
			{
				y += 2.0f;
				size -= 1.0f;
				if( size <= 0.0f ) enabled = false;
			}
			else
			{
				
				if( mode == Mode.DENSITY )
				{
					speed += ( 0.2f * time ) / size;
				}
				else if( mode == Mode.GRAVITY )
				{
					speed += ( 0.02f * Math.sqrt( time ) * Math.sqrt( size ) );
				}
				
				this.y += speed;
			}

			break;
			
			//antigravity can move things up or down and also slightly modify their sizes
		case ANTIGRAVITY:
			if( isOutOfBounds() || size < 0 ) enabled = false;
			else
			{
				speed -= ( rnd.nextFloat() - 0.5f ) * 2;
				y += speed;
				size += ( rnd.nextFloat() - 0.5f ) * 5;
				x += rnd.nextInt(2) == 0 ? -1 : 1;
			}
			
			break;

			//evaporate shrinks the circles until they dissapear.
		case EVAPORATE:
			
			this.size--;
			if( size <= 0 ) enabled = false;
			
			break;
			
			//seizure mode: for advanced users only!
		case SEIZURE:
			color = Color.rgb( rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256) );
			this.size = rnd.nextFloat() * 200.0f;
			
			switch( rnd.nextInt( 5 ) )
			{
			case 0:
				if( y >= maxY - size )
				{
					y += 2.0f;
					size -= 1.0f;
					if( size <= 0.0f ) enabled = false;
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
			
			speed += ( 0.2f * time * size ) / ( size * size );
			this.y += speed;
			
			break;
		}
		return enabled;
	}
	

	
	protected boolean isOutOfBounds()
	{
		//too large?
		if( y > maxY || x > maxX  ) return true;
		//too small?
		if( y < 0 || x < 0 ) return true;
		
		//we're good.
		return false;
	}
}
