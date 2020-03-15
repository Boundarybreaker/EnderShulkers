package space.bbkr.endershulkers;

import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Iterator;
import java.util.List;

public interface DyeableBlockEntity {

	boolean hasColor();

	int getColor();

	void setColor(int color);

	void removeColor();

	default void blendAndSetColor(List<DyeItem> colors) {
		int[] is = new int[3];
		int i = 0;
		int j = 0;
		int o;
		float r;
		int n;
		if (hasColor()) {
			o = getColor();
			float f = (float)(o >> 16 & 255) / 255.0F;
			float g = (float)(o >> 8 & 255) / 255.0F;
			r = (float)(o & 255) / 255.0F;
			i = (int)((float)i + Math.max(f, Math.max(g, r)) * 255.0F);
			is[0] = (int)((float)is[0] + f * 255.0F);
			is[1] = (int)((float)is[1] + g * 255.0F);
			is[2] = (int)((float)is[2] + r * 255.0F);
			++j;
		}

		for(Iterator<DyeItem> var14 = colors.iterator(); var14.hasNext(); ++j) {
			DyeItem dyeItem = var14.next();
			float[] fs = dyeItem.getColor().getColorComponents();
			int l = (int)(fs[0] * 255.0F);
			int m = (int)(fs[1] * 255.0F);
			n = (int)(fs[2] * 255.0F);
			i += Math.max(l, Math.max(m, n));
			is[0] += l;
			is[1] += m;
			is[2] += n;
		}


		o = is[0] / j;
		int p = is[1] / j;
		int q = is[2] / j;
		r = (float)i / (float)j;
		float s = (float)Math.max(o, Math.max(p, q));
		o = (int)((float)o * r / s);
		p = (int)((float)p * r / s);
		q = (int)((float)q * r / s);
		n = (o << 8) + p;
		n = (n << 8) + q;
		setColor(n);
	}
}
