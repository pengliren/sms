package com.sms.io.utils;

import java.io.PrintWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOM2Writer {

	private static Logger logger = LoggerFactory.getLogger(DOM2Writer.class);

	/**
	 * Serialize this node into the writer as XML.
	 * 
	 * @param writer Writer object
	 * @param node  DOM node
	 */
	public static void serializeAsXML(Node node, Writer writer) {
		PrintWriter out = new PrintWriter(writer);
		print(node, out);
		out.flush();
	}

	/**
	 * Dumps DOM node
	 * 
	 * @param node
	 *            Node to dump
	 * @param out
	 *            Writer object
	 */
	private static void print(Node node, PrintWriter out) {
		if (node == null) {
			return;
		}

		boolean hasChildren = false;
		int type = node.getNodeType();
		NodeList children = null;
		switch (type) {
			case Node.DOCUMENT_NODE:
				children = node.getChildNodes();
				if (children != null) {
					int numChildren = children.getLength();
					for (int i = 0; i < numChildren; i++) {
						print(children.item(i), out);
					}
				}
				break;
			case Node.ELEMENT_NODE:
				out.print('<');
				out.print(node.getNodeName());
				if (node.hasAttributes()) {
					NamedNodeMap attrs = node.getAttributes();
					int len = (attrs != null) ? attrs.getLength() : 0;
					for (int a = 0; a < len; a++) {
						Attr attr = (Attr) attrs.item(a);
						out.print(' ');
						out.print(attr.getNodeName());
						out.print("=\"");
						out.print(attr.getValue());
						out.print('\"');
					}
				}
				children = node.getChildNodes();
				if (children != null) {
					int numChildren = children.getLength();
					hasChildren = (numChildren > 0);
					if (hasChildren) {
						out.print('>');
					}
					for (int i = 0; i < numChildren; i++) {
						print(children.item(i), out);
					}
				} else {
					hasChildren = false;
				}
				if (!hasChildren) {
					out.print("/>");
				}
				break;
			case Node.ENTITY_REFERENCE_NODE:
				out.print('&');
				out.print(node.getNodeName());
				out.print(';');
				break;
			case Node.CDATA_SECTION_NODE:
				out.print("<![CDATA[");
				out.print(node.getNodeValue());
				out.print("]]>");
				break;
			case Node.TEXT_NODE:
				out.print(node.getNodeValue());
				break;
			default:
				if (logger.isDebugEnabled()) {
					logger.debug("Unknown type: " + type);
				}
		}
		if (type == Node.ELEMENT_NODE && hasChildren) {
			out.print("</");
			out.print(node.getNodeName());
			out.print('>');
			hasChildren = false;
		}
	}
}
