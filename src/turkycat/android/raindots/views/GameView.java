package turkycat.android.raindots.views;

import java.util.Random;

import turkycat.android.raindots.drawables.DrawableCircle;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class GameView extends SurfaceView implements SurfaceHolder.Callback
{
	//-----------------------------------------------------------------items used by this class
	protected enum Status
	{
		RUNNING, PAUSED
	};

	public enum Mode
	{
		GRAVITY, ANTIGRAVITY, DENSITY, EVAPORATE, PAINT, WTF
	};
	
	public static final String TAG = "GameView";
	
	private GameThread gameThread;
	private boolean started = false;
	
	
	//------------------------------------------------------------------constructors

	public GameView(Context context)
	{
		super( context );
		init( context );
	}

	public GameView(Context context, AttributeSet attrs)
	{
		super( context, attrs );
		init( context );
	}

	public GameView(Context context, AttributeSet attrs, int defStyle)
	{
		super( context, attrs, defStyle );
		init( context );
	}

	
	//---------------------------------------------------------------------------methods
	
	private void init( Context context )
	{
		SurfaceHolder holder = getHolder();
		holder.addCallback( this );
		//mGameThread = new GameThread( holder, context );
	}
	
	
	@Override
	public boolean onTouchEvent( MotionEvent event )
	{
		return gameThread.handleTouchEvent( event );
	}
	
	

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		gameThread.setScreenDimensions( width, height );
		Log.d( TAG,  "surface changed" );
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0)
	{
		Log.i( TAG, "Surface Created." );
		gameThread = new GameThread( getHolder(), getContext() );
		gameThread.setRunning( true );
		if( !started )
		{
			gameThread.start();
			started = true;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0)
	{
		Log.i( TAG, "Surface destroyed." );
		gameThread.setRunning( false );
		try
		{
			gameThread.join();
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
	}
	
	
	
	public void setMode( Mode mode )
	{
		gameThread.setMode( mode );
	}
	
	
	public class GameThread extends Thread
	{
		private SurfaceHolder holder;
		private Context context;
		
		//enums for control
		private Status status;
		private Mode mode;
		private boolean running;
		
		public int screenMaxY;
		public int screenMaxX;
		
		Random rnd;
		
		private DrawableCircle[] circles;
		private int start;
		private int end;
		
		public GameThread( SurfaceHolder holder, Context context )
		{
			this.holder = holder;
			this.context = context;
			this.rnd = new Random();
			status = Status.RUNNING;
			mode = Mode.EVAPORATE;
			circles = new DrawableCircle[1000];
			start = 0;
			end = 0;
			
			//get the maximum pixels of the screen
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			Point p = new Point();
			display.getSize( p );
			setScreenDimensions( p.x, p.y );
		}
		
		public void setScreenDimensions( int x, int y )
		{
			this.screenMaxY = y;
			this.screenMaxX = x;
		}
		
		
		public boolean handleTouchEvent(MotionEvent event)
		{
			int size = event.getPointerCount();
			for( int i = 0; i < size; i++ )
			{
				float x = event.getX(i);
				float y = event.getY(i);
				circles[end] = new DrawableCircle(x, y, screenMaxX, screenMaxY);
				end = (end + 1) % circles.length;
			}
			return true;
		}

		
		public void setMode( Mode mode )
		{
			this.mode = mode;
		}
		
		
		@Override
		public void run()
		{
			while( status == Status.RUNNING )
			{
				synchronized( holder )
				{
					if( status == Status.RUNNING )
					{
						Canvas canvas = holder.lockCanvas( null );
						if( canvas != null )
						{
							if( mode == Mode.WTF )
								canvas.drawColor( Color.rgb( rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256) ) );
							else if( mode != Mode.PAINT )
								canvas.drawColor( Color.BLACK );
							update();
							drawCircles( canvas );
							holder.unlockCanvasAndPost( canvas );
						}
					}
				}
			}
		}


		public void setRunning( boolean running )
		{
			this.status = running ? Status.RUNNING : Status.PAUSED;
		}
		
		
		

		private void drawCircles(Canvas canvas)
		{
			int i;
			//evaluate once, use many times;
			int first_term = end < start ? circles.length : end;
			Log.i( TAG, "start = " + start + " termination = " + first_term );
			
			for ( i = start; i < first_term; i++ )
			{
				circles[i].draw( canvas );
			}
			
			if( end < start )
			{
				Log.i( TAG, "entered 2nd loop." );
				for( i = 0; i < end; i++ )
				{
					circles[i].draw( canvas );
				}
			}
		}

		private void update()
		{
			int i;
			int stopping_point = end < start ? circles.length : end;
			
			for ( i = start; i < stopping_point && !circles[i].update( mode ); i++ ) start = ( start + 1 ) % circles.length;
			for ( i++; i < stopping_point; i++ ) circles[i].update( mode );
			
			if( end < start )
				for( i = 0; i < end; i++ ) circles[i].update( mode );
		}
	}

}
