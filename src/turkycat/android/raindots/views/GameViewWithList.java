package turkycat.android.raindots.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import turkycat.android.raindots.drawables.DrawableCircle;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameViewWithList extends SurfaceView implements SurfaceHolder.Callback
{
	//-----------------------------------------------------------------items used by this class
	protected enum Mode
	{
		RUNNING, PAUSED
	};
	
	public static final String TAG = "GameView";
	
	private GameThread mGameThread;
	
	
	//------------------------------------------------------------------constructors

	public GameViewWithList(Context context)
	{
		super( context );
		init( context );
	}

	public GameViewWithList(Context context, AttributeSet attrs)
	{
		super( context, attrs );
		init( context );
	}

	public GameViewWithList(Context context, AttributeSet attrs, int defStyle)
	{
		super( context, attrs, defStyle );
		init( context );
	}

	
	//---------------------------------------------------------------------------methods
	
	private void init( Context context )
	{
		SurfaceHolder holder = getHolder();
		holder.addCallback( this );
	}
	
	
	@Override
	public boolean onTouchEvent( MotionEvent event )
	{
		return mGameThread.handleTouchEvent( event );
	}
	
	

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
	{
		Log.d( TAG,  "surface changed" );
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0)
	{
		mGameThread = new GameThread( getHolder(), getContext() );
		mGameThread.setRunning( true );
		mGameThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0)
	{
		mGameThread.setRunning( false );
		try
		{
			mGameThread.join();
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
	}
	
	
	
	
	public class GameThread extends Thread
	{
		private SurfaceHolder holder;
		private Context context;
		private Mode mode;
		private List<DrawableCircle> circles = new ArrayList<DrawableCircle>();
		
		public GameThread( SurfaceHolder holder, Context context )
		{
			this.holder = holder;
			this.context = context;
			mode = Mode.RUNNING;
			circles = new LinkedList<DrawableCircle>();
		}
		
		
		public boolean handleTouchEvent( MotionEvent event )
		{
			float x = event.getX();
			float y = event.getY();
			circles.add( new DrawableCircle( x, y ) );
			return true;
		}
		
		
		@Override
		public void run()
		{
			while( mode == Mode.RUNNING )
			{
				Canvas canvas = holder.lockCanvas( null );
				synchronized( holder )
				{
					if( mode == Mode.RUNNING )
					{
						canvas.drawColor( Color.BLACK );
						update();
						doDraw( canvas );
					}
				}
				holder.unlockCanvasAndPost( canvas );
			}
		}

		private void doDraw(Canvas canvas)
		{
			//evaluate once, use many times;
			int size = circles.size();
			for ( int i = 0; i < size; i++ )
			{
				circles.get( i ).draw( canvas );
			}
		}

		private void update()
		{
			//evaluate once, use many times;
			int size = circles.size();
			Iterator<DrawableCircle> iter = circles.iterator();
			while( iter.hasNext() )
			{
				DrawableCircle cir = iter.next();
				//if( !cir.update() ) iter.remove();
			}
		}

		public void setRunning( boolean running )
		{
			this.mode = running ? Mode.RUNNING : mode.PAUSED;
		}
	}

}
