package turkycat.android.raindots.views;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

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
//	protected enum Status
//	{
//		RUNNING, PAUSED
//	};

	public enum Mode
	{
		GRAVITY, ANTIGRAVITY, DENSITY, EVAPORATE, PAINT, SEIZURE
	};
	
	public static final String TAG = "GameView";
	
	private GameThread gameThread;
	
	
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
		gameThread.start();
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
		
		private Mode mode;
		private boolean running;
		
		public int screenMaxY;
		public int screenMaxX;
		
		Random rnd;
		
		private ConcurrentLinkedQueue<DrawableCircle> circles;
		//private LinkedList<DrawableCircle> circles;
		
		public GameThread( SurfaceHolder holder, Context context )
		{
			this.holder = holder;
			this.context = context;
			this.rnd = new Random();
			this.running = true;
			mode = Mode.EVAPORATE;
			
			//choosing to use manual synchronization, rather than using a concurrent class
			circles = new ConcurrentLinkedQueue<DrawableCircle>();
			//circles = new LinkedList<DrawableCircle>();
			
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
			
			Log.i( TAG, "reached touch event" );
			
			//size should never be more than 4 or 5 realistically. So the loop internal to this sync
			//should be relatively fast. Lock time is still minimized
			synchronized( circles )
			{
				for( int i = 0; i < size; i++ )
				{
					float x = event.getX( i );
					float y = event.getY( i );
					circles.add( new DrawableCircle( x, y, screenMaxX, screenMaxY ) );
					Log.i( TAG, "circle created at " + x + " " + y );
				}
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
			while( running )
			{
				synchronized( holder )
				{
					Canvas canvas = holder.lockCanvas( null );
					if( canvas != null )
					{
						//draw a random color for seizure mode, clear the screen unless using paint mode.
						if( mode == Mode.SEIZURE ) canvas.drawColor( Color.rgb( rnd.nextInt( 256 ), rnd.nextInt( 256 ), rnd.nextInt( 256 ) ) );
						else if( mode != Mode.PAINT ) canvas.drawColor( Color.BLACK );
						
						//to save time in paint mode, don't update circles.
						if( mode != Mode.PAINT ) update();
						drawCircles( canvas );
						holder.unlockCanvasAndPost( canvas );
					}
				}
			}
		}


		public void setRunning( boolean running )
		{
			this.running = running;
		}
		
		
		

		private void drawCircles(Canvas canvas)
		{
			synchronized( circles )
			{
				//Iterator<DrawableCircle> iter = circles.iterator();
				for( DrawableCircle circle : circles )
				{
					circle.draw( canvas );
				}
			}
		}

		private void update()
		{
			synchronized( circles )
			{
				Iterator<DrawableCircle> iter = circles.iterator();
				while( iter.hasNext() )
				{
					if( !iter.next().update( mode ) ) iter.remove();
				}
			}
		}
	}

}
