package at.jku.isse.cloud.artifact;

import static java.util.Objects.requireNonNull;

public class DSLink {
	
	public final String name;
	public final DSClass target;
	public final int srcMin, srcMax, dstMin, dstMax;

	public DSLink(String name, DSClass target, int srcMin, int srcMax, int dstMin,
			int dstMax) {
		this.name = requireNonNull(name);
		this.target = requireNonNull(target);
		this.srcMin = srcMin;
		this.srcMax = srcMax;
		this.dstMin = dstMin;
		this.dstMax = dstMax;
	}
}
