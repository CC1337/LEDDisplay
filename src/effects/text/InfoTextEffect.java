package effects.text;

import net.NetIO1OG;
import net.PvData;
import led.ILEDArray;
import effects.*;

public class InfoTextEffect implements IColorableEffect {
	
	private enum State {
		Start,
		Temperatures,
		PVInfo,
		News,
		Weather
	}
	
	private IPixelatedFont _font;
	private IColor _color;
	private byte[][] _textArray;
	private int _posX;
	private int _posY;
	private State _state = State.Start;
	private int _stateProgress = 2;
	private boolean _switchToNextInfo = false;
	private int _animationState = 0;
	private byte[][] _prevStateData;
	private byte[][] _nextStateData;
	private NetIO1OG _netIO1OGData = NetIO1OG.getInstance();
	private PvData _pvData = PvData.getInstance();
	
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
	
	/**
	 * true every time a loop ended. Note: only between to calls to nixtInfo()
	 * @return true if currently in last state of the info loop
	 */
	public boolean loopEnded() {
		return _state == State.Start && _stateProgress < 2;
	}
	
	public void toStart() {
		_state = State.Start;
		_stateProgress = 2;
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
		if (_state == State.Start) {
			_stateProgress++;
			if (_stateProgress >= 3) {
				_state = State.Temperatures;
				_stateProgress = 0;
				processState();
				return;
			} else {
				_prevStateData = _nextStateData;
				_nextStateData = _font.toPixels(" ");
				_animationState = 1;
			}
		}
		if (_state == State.Temperatures) {
			_prevStateData = _nextStateData;
			_animationState = 1;
			if (_stateProgress == 0) {
				_nextStateData = _font.toPixels("SZ: " + _netIO1OGData.getTempSchlafzimmer() + "\u00BA");
			}
			if (_stateProgress == 1) {
				_nextStateData = _font.toPixels("WZ: " + _netIO1OGData.getTempWohnzimmer() + "\u00BA");
			}
			if (_stateProgress == 2) {
				_nextStateData = _font.toPixels("AQ: " + _netIO1OGData.getTempAquarium() + "\u00BA");
			}
			if (_stateProgress == 3) {
				_nextStateData = _font.toPixels("Ba: " + _netIO1OGData.getTempBalkon() + "\u00BA");
			}
			if (_stateProgress == 4) {
				_nextStateData = _font.toPixels("Au: " + _netIO1OGData.getTempAussenKueche() + "\u00BA");
			}
			if (_stateProgress == 5) {
				_nextStateData = _font.toPixels(printOptionalSpace(_netIO1OGData.getTempAussenKuecheMin(), 4) + _netIO1OGData.getTempAussenKuecheMin() + "-" + _netIO1OGData.getTempAussenKuecheMax() + "\u00BA");
			}
			_stateProgress = ++_stateProgress % 7;
			if (_stateProgress == 0) {
				_state = State.PVInfo;
				processState();
				return;
			}
		}
		
		if (_state == State.PVInfo) {
			_prevStateData = _nextStateData;
			_animationState = 1;
			
			int currentPac = _pvData.getCurrentPac();
			double kwhDay = _pvData.getKwhDay();
			int currentD0Pac = _pvData.getCurrentD0Pac();
			int currentSelfConsumption = _pvData.getCurrentSelfConsumption();
			int currentOverallConsumption = _pvData.getCurrentOverallConsumption();
			double drawnKwh = _pvData.getDrawnKwhDay();
			double suppliedKwh = _pvData.getSuppliedKwhDay();
			double overallConsumedKwhDay = _pvData.getOverallConsumedKwhDay();
			double dayDiff = _pvData.getDayExpectedDiff();
			int selfConsumedKwhDayInt = (int) Math.round(_pvData.getSelfConsumedKwhDay());
			int selfConsumptionRatioDay = _pvData.getSelfConsumptionRatioDay();
			
			if (_stateProgress == 0) {
				if (currentPac > 0)
					_nextStateData = _font.toPixels("PAC:" + currentPac + "W");
				else _stateProgress++;
			} 
			if (_stateProgress == 1) {
				_nextStateData = _font.toPixels("Max W:" + _pvData.getMaxPacDay());
			}
			if (_stateProgress == 2) {
				_nextStateData = _font.toPixels("Prod:" + printOptionalSpace(kwhDay, 10) + String.format("%.1f", kwhDay));
			}
			if (_stateProgress == 3) {
				
				_nextStateData = _font.toPixels("Soll" + printOptionalSpace(dayDiff, 10) + (dayDiff >= 0 ? "+" : "") + String.format("%.1f", dayDiff));
			}
			if (_stateProgress == 4) {
				_nextStateData = _font.toPixels("\u2195:" + currentD0Pac + "W");
			}
			if (_stateProgress == 5) {
				if (currentSelfConsumption > 0)
					_nextStateData = _font.toPixels("EV:" + currentSelfConsumption + "W");
				else _stateProgress++;
			}
			if (_stateProgress == 6) {
				_nextStateData = _font.toPixels("EV:" + printOptionalSpace(selfConsumedKwhDayInt, 10) + selfConsumedKwhDayInt + " " + selfConsumptionRatioDay + "%");
			}
			if (_stateProgress == 7) {
				_nextStateData = _font.toPixels("VB:" + currentOverallConsumption + "W");
			}
			if (_stateProgress == 8) {
				_nextStateData = _font.toPixels("VB \u2211:" + printOptionalSpace(overallConsumedKwhDay, 10) + String.format("%.1f", overallConsumedKwhDay));
			}
			if (_stateProgress == 9) {
				_nextStateData = _font.toPixels("\u2191" + printOptionalSpace(suppliedKwh, 10) + String.format("%.1f", suppliedKwh) + 
						"\u2193" + printOptionalSpace(drawnKwh, 10) + String.format("%.1f", drawnKwh));
			}
			_stateProgress = ++_stateProgress % 11;
			if (_stateProgress == 0) {
				_state = State.Start;
				processState();
				return;
			}
		}
	}
	
	private void changeAnimation() {
		if (_textArray == null || _prevStateData == null)
			return;
		
		if (_state == State.Temperatures || _state == State.PVInfo || _state == State.Start) {
			for(int x=0; x<_textArray.length; x++) {
				for(int y=0; y<_textArray[0].length; y++) {
					if (y + _animationState >= _textArray[0].length) { 
						if (x >= _nextStateData.length) {
							_textArray[x][y] = 0;
						} else {
							_textArray[x][y] = _nextStateData[x][-_textArray[0].length + y + _animationState];
						}
					} else {
						if (x >= _prevStateData.length) {
							_textArray[x][y] = 0;
						} else {
							_textArray[x][y] = _prevStateData[x][y + _animationState];
						}
					}
				}
			}
			_animationState = ++_animationState % 8;
		}
		
	}
	
	private String printOptionalSpace(double val, int thresholdVal) {
		return Math.abs(val) >= thresholdVal ? "" : " ";
	}
	
	private String printOptionalSpace(String val, int thresholdLength) {
		return val.length() >= thresholdLength ? "" : " ";
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
