package turkycat.android.raindots.views;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import turkycat.android.raindots.R;
import turkycat.android.raindots.application.Raindots;
import turkycat.android.raindots.drawables.DrawableBitmap;
import turkycat.android.raindots.drawables.DrawableCircle;
import turkycat.android.raindots.drawables.DrawableItem;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	public enum Mode
	{
		GRAVITY, ANTIGRAVITY, DENSITY, EVAPORATE, PAINT, SEIZURE
	};
	
	public static final String TAG = "GameView";
	
	private Raindots application;
	private GameThread gameThread;
	private HashMap<String, Bitmap> bitmaps;
	
	
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
		this.bitmaps = new HashMap<String, Bitmap>();
		
		//register this as a callback for the surfaceholder
		SurfaceHolder holder = getHolder();
		holder.addCallback( this );
		
		//add bitmap resources to dictionary
		Resources res = getResources();
		Bitmap bitmap = BitmapFactory.decodeResource( res, R.drawable.droidbro );
		bitmap = Bitmap.createScaledBitmap( bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, false );
		bitmaps.put( "droid", bitmap );
		
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
	
	
	/**
	 * sets the mode for the drawing thread
	 */
	public void setMode( Mode mode )
	{
		gameThread.setMode( mode );
	}
	
	
	/**
	 * toggles the drawable type on the game thread
	 */
	public void toggleDrawable()
	{
		gameThread.toggleDrawable();
	}
	
	
	public class GameThread extends Thread
	{
		private SurfaceHolder holder;
		private Context context;
		
		private Mode mode;
		private boolean running;
		private boolean drawBitmaps = false;
		
		public int screenMaxY;
		public int screenMaxX;
		
		Random rnd;
		
		private ConcurrentLinkedQueue<DrawableItem> items;
		//private LinkedList<DrawableCircle> items;
		
		public GameThread( SurfaceHolder holder, Context context )
		{
			this.holder = holder;
			this.context = context;
			this.rnd = new Random();
			this.running = true;
			mode = Mode.EVAPORATE;
			
			//can use linked list with manual synchronization, rather than using a concurrent class
			//but grabbing the locks before I do things with them doesn't hurt much if at all.
			items = new ConcurrentLinkedQueue<DrawableItem>();
			//items = new LinkedList<DrawableCircle>();
			
			//get the maximum pixels of the screen
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			Point p = new Point();
			display.getSize( p );
			setScreenDimensions( p.x, p.y );
		}
		
		/**
		 * sets the screen dimensions for the view
		 */
		public void setScreenDimensions( int x, int y )
		{
			this.screenMaxY = y;
			this.screenMaxX = x;
		}
		
		
		/**
		 * handles the touch events by creating new items to draw
		 */
		public boolean handleTouchEvent(MotionEvent event)
		{
			int size = event.getPointerCount();
			
			//Log.i( TAG, "reached touch event" );
			
			//size should never be more than 4 or 5 realistically. So the loop internal to this sync
			//should be relatively fast. Lock time is still minimized
			synchronized( items )
			{
				for( int i = 0; i < size; i++ )
				{
					float x = event.getX( i );
					float y = event.getY( i );
					
					if( drawBitmaps )
					{
						items.add( new DrawableBitmap( bitmaps.get( "droid" ), x, y, rnd.nextInt(100) + 1, screenMaxX, screenMaxY ) );
					}
					else
					{
						items.add( new DrawableCircle( x, y, screenMaxX, screenMaxY ) );
					}
					
					//Log.i( TAG, "item created at " + x + " " + y );
				}
			}
			return true;
		}

		
		/**
		 * sets the mode
		 */
		public void setMode( Mode mode )
		{
			this.mode = mode;
		}

		
		
		/**
		 * toggles the drawable type
		 */
		public void toggleDrawable()
		{
			drawBitmaps = !drawBitmaps;
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
						
						//to save time in paint mode, don't update items.
						if( mode != Mode.PAINT ) update();
						drawItems( canvas );
						holder.unlockCanvasAndPost( canvas );
					}
				}
			}
		}


		public void setRunning( boolean running )
		{
			this.running = running;
		}
		
		
		

		private void drawItems(Canvas canvas)
		{
			synchronized( items )
			{
				//Iterator<DrawableCircle> iter = items.iterator();
				for( DrawableItem item : items )
				{
					item.draw( canvas );
				}
			}
		}

		private void update()
		{
			synchronized( items )
			{
				Iterator<DrawableItem> iter = items.iterator();
				while( iter.hasNext() )
				{
					if( !iter.next().update( mode ) ) iter.remove();
				}
			}
		}
	}

}
