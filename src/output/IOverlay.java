package output;

import effects.IEffect;

public interface IOverlay {

	void addOverlay(IEffect overlayEffect);
	
	void addOverlay(IEffect overlayEffect, int overlayDuratrionMs);
}
