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
	private NetIO1OG _netIO1OGData = NetIO1OG.getInstance();
	
	public InfoTextEffect(IPixelatedFont font, IColor color, int posX, int posY) {
		_font = font;
		setPosX(posX);
		setPosY(posY);
		_color = color;
		updateData();
		nextInfo();
	}
	
	public InfoTextEffect(IPixelatedFont font, IColor color) {
		this(font, color, 0, 0);
	}
	
	
	public void updateData() {
		
	}
	
	public void nextInfo() {
		if (_state == State.Temperatures) {
			if (_stateProgress == 0)
				_textArray = _font.toPixels("Au: " + _netIO1OGData.getTempAussenKueche() + "\u00BA");
			if (_stateProgress == 1)
				_textArray = _font.toPixels("SZ: " + _netIO1OGData.getTempSchlafzimmer() + "\u00BA");
			if (_stateProgress == 2)
				_textArray = _font.toPixels("WZ: " + _netIO1OGData.getTempWohnzimmer() + "\u00BA");
			if (_stateProgress == 3)
				_textArray = _font.toPixels("AQ: " + _netIO1OGData.getTempAquarium() + "\u00BA");
			if (_stateProgress == 4)
				_textArray = _font.toPixels("Ba: " + _netIO1OGData.getTempBalkon() + "\u00BA");
			
			_stateProgress++;
			if (_stateProgress >= 5) 
				_stateProgress = 0;
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
