// Copyright (c) 2003-2012, Jodd Team (jodd.org). All Rights Reserved.

package jodd.lagarto.dom;

import jodd.lagarto.LagartoParserEngine;
import jodd.util.StringUtil;

import java.nio.CharBuffer;

/**
 * Lagarto DOM builder creates DOM tree from HTML, XHTML or XML content.
 */
public class LagartoDOMBuilder extends LagartoParserEngine {

	public LagartoDOMBuilder() {
		enableHtmlMode();
	}

	/**
	 * Default void tags.
	 * http://dev.w3.org/html5/spec/Overview.html#void-elements
	 */
	public static final String[] HTML5_VOID_TAGS = {
			"area", "base", "br", "col", "command", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"};

	protected boolean ignoreWhitespacesBetweenTags;
	protected boolean caseSensitive;
	protected boolean ignoreComments;
	protected boolean selfCloseVoidTags;
	protected boolean collectErrors;
	protected String conditionalCommentExpression;
	protected String[] voidTags = HTML5_VOID_TAGS;

	public boolean isIgnoreWhitespacesBetweenTags() {
		return ignoreWhitespacesBetweenTags;
	}

	/**
	 * Specifies if whitespaces between open/closed tags should be ignored.
	 */
	public void setIgnoreWhitespacesBetweenTags(boolean ignoreWhitespacesBetweenTags) {
		this.ignoreWhitespacesBetweenTags = ignoreWhitespacesBetweenTags;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * Specifies if tag names are case sensitive.
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public boolean isIgnoreComments() {
		return ignoreComments;
	}

	/**
	 * Specifies if comments should be ignored in DOM tree.
	 */
	public void setIgnoreComments(boolean ignoreComments) {
		this.ignoreComments = ignoreComments;
	}

	public String[] getVoidTags() {
		return voidTags;
	}

	/**
	 * Sets void tags. If <code>null</code>, void tags are not used.
	 */
	public void setVoidTags(String... voidTags) {
		this.voidTags = voidTags;
	}

	/**
	 * Returns <code>true</code> if void tags are used.
	 * Using void tags makes parsing a different.
	 */
	public boolean hasVoidTags() {
		return voidTags != null;
	}

	/**
	 * Returns <code>true</code> if tag name is void.
	 * If void tags are not defined, returns <code>false</code>
	 * for any input.
	 */
	public boolean isVoidTag(String tagName) {
		if (voidTags == null) {
			return false;
		}
		tagName = tagName.toLowerCase();
		return StringUtil.equalsOne(tagName, voidTags) != -1;
	}

	public boolean isSelfCloseVoidTags() {
		return selfCloseVoidTags;
	}

	/**
	 * Specifies if void tags should be self closed.
	 */
	public void setSelfCloseVoidTags(boolean selfCloseVoidTags) {
		this.selfCloseVoidTags = selfCloseVoidTags;
	}

	public boolean isCollectErrors() {
		return collectErrors;
	}

	/**
	 * Enables error collection during parsing.
	 */
	public void setCollectErrors(boolean collectErrors) {
		this.collectErrors = collectErrors;
	}

	public String getConditionalCommentExpression() {
		return conditionalCommentExpression;
	}

	public void setConditionalCommentExpression(String conditionalCommentExpression) {
		this.conditionalCommentExpression = conditionalCommentExpression;
	}

	// ---------------------------------------------------------------- quick settings

	/**
	 * Enables HTML5 parsing mode.
	 */
	public LagartoDOMBuilder enableHtmlMode() {
		ignoreWhitespacesBetweenTags = false;			// collect all whitespaces
		caseSensitive = false;							// HTML is case insensitive
		parseSpecialTagsAsCdata = true;					// script and style tags are parsed as CDATA
		voidTags = HTML5_VOID_TAGS;						// list of void tags
		selfCloseVoidTags = false;						// don't self close void tags
		enableConditionalComments = true;				// enable IE conditional comments
		conditionalCommentExpression = "if !IE";		// treat HTML as non-IE browser
		return this;
	}

	/**
	 * Enables XHTML mode.
	 */
	public LagartoDOMBuilder enableXhtmlMode() {
		ignoreWhitespacesBetweenTags = false;			// collect all whitespaces
		caseSensitive = true;							// XHTML is case sensitive
		parseSpecialTagsAsCdata = false;				// all tags are parsed in the same way
		voidTags = HTML5_VOID_TAGS;						// list of void tags
		selfCloseVoidTags = true;						// self close void tags
		enableConditionalComments = true;				// enable IE conditional comments
		conditionalCommentExpression = "if !IE";		// treat HTML as non-IE browser
		return this;
	}

	/**
	 * Enables XML parsing mode.
	 */
	public LagartoDOMBuilder enableXmlMode() {
		ignoreWhitespacesBetweenTags = true;			// ignore whitespaces that are non content
		caseSensitive = true;							// XML is case sensitive
		parseSpecialTagsAsCdata = false;				// all tags are parsed in the same way
		voidTags = null;								// there are no void tags
		selfCloseVoidTags = false;						// don't self close empty tags (can be changed!)
		enableConditionalComments = false;				// disable IE conditional comments
		conditionalCommentExpression = null;			// treat HTML as non-IE browser
		return this;
	}

	// ---------------------------------------------------------------- parse

	/**
	 * Creates DOM tree from provided content.
	 */
	public Document parse(CharSequence content) {
		initialize(CharBuffer.wrap(content));
		return doParse();
	}

	/**
	 * Creates DOM tree from the provided content.
	 */
	public Document parse(CharBuffer content) {
		initialize(content);
		return doParse();
	}

	/**
	 * Parses the content.
	 */
	protected Document doParse() {
		DOMBuilderTagVisitor domBuilderTagVisitor = createDOMDomBuilderTagVisitor();

		parse(domBuilderTagVisitor);

		return domBuilderTagVisitor.getDocument();
	}

	/**
	 * Creates {@link DOMBuilderTagVisitor}.
	 */
	protected DOMBuilderTagVisitor createDOMDomBuilderTagVisitor() {
		return new DOMBuilderTagVisitor(this);
	}

}
