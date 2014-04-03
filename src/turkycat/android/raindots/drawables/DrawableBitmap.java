package turkycat.android.raindots.drawables;

import turkycat.android.raindots.views.GameView.Mode;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;

public class DrawableBitmap extends DrawableItem
{
	private static final String TAG = "DrawableItem";
	
	private Bitmap bitmap;
	
	public DrawableBitmap( Bitmap bitmap, float x, float y )
	{
		this( bitmap, x, y, 100, Integer.MAX_VALUE, Integer.MAX_VALUE );
	}
	
	public DrawableBitmap( Bitmap bitmap, float x, float y, float size )
	{
		this( bitmap, x, y, size, Integer.MAX_VALUE, Integer.MAX_VALUE );
	}
	
	public DrawableBitmap( Bitmap bitmap, float x, float y, float size, int maxX, int maxY )
	{
		init( x, y, maxX, maxY, false );
		
		//clamp size [0,1]
		this.size = Math.max( 0f, Math.min( 100f, size ) );
		
		this.bitmap = bitmap;
	}

	@Override
	public boolean update( Mode mode )
	{
		return super.update( mode );
	}

	@Override
	public void draw( Canvas canvas )
	{
		Log.i( TAG, "bitmap being drawn? " + (bitmap == null ? "NO" : "YES" ) );
		Matrix matrix = new Matrix();
		matrix.postScale( size / 100f, size / 100f );
		matrix.postTranslate( x, y );
		canvas.drawBitmap( bitmap, matrix, null );
	}
}
