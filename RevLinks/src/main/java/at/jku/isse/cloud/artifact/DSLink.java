package at.jku.isse.cloud.artifact;

import static java.util.Objects.requireNonNull;

/**
 * Represents a link between artifacts in the design space. A link is defined by its name,
 * the target artifact (contained in an DSClass object) and the cardinalities between the source and the target.
 * @author Gabriel Schoerghuber
 * @author Dominik Steinbinder
 */
public class DSLink {
	
	public final String name;
	public final DSClass target;
	public final int srcMin, srcMax, dstMin, dstMax;

	/**
	 * Creates a new DSLink object.
	 * @param name the name of the link
	 * @param target the DSClass that contains the target artifact
	 * @param srcMin minimum number of source elements
	 * @param srcMax maximum number of source elements
	 * @param dstMin minimum number of destination elements
	 * @param dstMax maximum number of destination elements
	 */
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
