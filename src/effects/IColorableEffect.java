package effects;

public interface IColorableEffect extends IEffect {

	public IColor getColor();
	public void setColor(IColor color);
	public byte[][] getEffectData();
}
