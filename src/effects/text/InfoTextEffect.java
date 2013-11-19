package effects.text;

import net.NetIO1OG;
import led.ILEDArray;
import effects.*;

enum State {
	Temperatures,
	PVInfo,
	News,
	Weather
}

public class InfoTextEffect implements IColorableEffect {
	
	private IPixelatedFont _font;
	private IColor _color;
	private byte[][] _textArray;
	private int _posX;
	private int _posY;
	private State _state = State.Temperatures;
	private int _stateProgress = 0;
	private boolean _switchToNextInfo = false;
	private int _animationState = 0;
	private byte[][] _prevStateData;
	private byte[][] _nextStateData;
	private NetIO1OG _netIO1OGData = NetIO1OG.getInstance();
	
	public InfoTextEffect(IPixelatedFont font, IColor color, int posX, int posY) {
		_font = font;
		setPosX(posX);
		setPosY(posY);
		_color = color;
		processState();
		nextInfo();
	}
	
	public InfoTextEffect(IPixelatedFont font, IColor color) {
		this(font, color, 0, 0);
	}
	
	public void nextInfo() {
		_switchToNextInfo = true;
	}
	
	private void nextFrame() {
		if (_animationState > 0)
			changeAnimation();
		
		if (!_switchToNextInfo)
			return;
		_switchToNextInfo = false;
		processState();	
	}
	
	private void processState() {
		if (_state == State.Temperatures) {
			_prevStateData = _nextStateData;
			if (_stateProgress == 0) {
				_nextStateData = _font.toPixels("Au: " + _netIO1OGData.getTempAussenKueche() + "\u00BA");
				_animationState = 1;
			}
			if (_stateProgress == 1) {
				_nextStateData = _font.toPixels("SZ: " + _netIO1OGData.getTempSchlafzimmer() + "\u00BA");
				_animationState = 1;
			}
			if (_stateProgress == 2) {
				_nextStateData = _font.toPixels("WZ: " + _netIO1OGData.getTempWohnzimmer() + "\u00BA");
				_animationState = 1;
			}
			if (_stateProgress == 3) {
				_nextStateData = _font.toPixels("AQ: " + _netIO1OGData.getTempAquarium() + "\u00BA");
				_animationState = 1;
			}
			if (_stateProgress == 4) {
				_nextStateData = _font.toPixels("Ba: " + _netIO1OGData.getTempBalkon() + "\u00BA");
				_animationState = 1;
			}

			_stateProgress = ++_stateProgress % 5;
		}
	}
	
	private void changeAnimation() {
		if (_textArray == null || _prevStateData == null)
			return;
		
		if (_state == State.Temperatures) {
			for(int x=0; x<_textArray.length; x++) {
				for(int y=0; y<_textArray[0].length; y++) {
					if (x >= _nextStateData.length || x >= _prevStateData.length) {
						_textArray[x][y] = 0;
					} else {
						//System.out.print("x: "+x + "  y: " + y + "  as: " + _animationState + "  ta0len: " + _textArray[0].length );
						
						if (y + _animationState >= _textArray[0].length) { 
							//System.out.println(" c1 " + (-_textArray[0].length + y + _animationState));
							_textArray[x][y] = _nextStateData[x][-_textArray[0].length + y + _animationState];
						} else {
							//System.out.println(" c2 " + (y + _animationState));
							_textArray[x][y] = _prevStateData[x][y + _animationState];
						}
							
					}
				}
			}
			_animationState = ++_animationState % 8;
		}
		
	}
	
	public int getPosX() {
		return _posX;
	}

	public void setPosX(int _posX) {
		this._posX = _posX;
	}

	public int getPosY() {
		return _posY;
	}

	public void setPosY(int _posY) {
		this._posY = _posY;
	}

	@Override
	public void apply(ILEDArray leds) {
		if (_textArray == null) {
			_textArray = new byte[leds.sizeX()][_font.toPixels(" ")[0].length];
			_prevStateData = new byte[leds.sizeX()][_font.toPixels(" ")[0].length];
		}
		nextFrame();
		_color.apply(leds, this);
	}

	@Override
	public IColor getColor() {
		return _color;
	}

	@Override
	public void setColor(IColor color) {
		_color = color;
	}

	@Override
	public byte[][] getEffectData() {
		return _textArray;
	}

}
