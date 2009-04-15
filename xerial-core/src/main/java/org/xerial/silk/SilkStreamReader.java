/*--------------------------------------------------------------------------
 *  Copyright 2009 Taro L. Saito
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *--------------------------------------------------------------------------*/
//--------------------------------------
// XerialJ
//
// SilkStreamReader.java
// Since: 2009/03/31 19:53:33
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.silk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.xerial.core.XerialError;
import org.xerial.core.XerialErrorCode;
import org.xerial.core.XerialException;
import org.xerial.json.JSONArray;
import org.xerial.json.JSONException;
import org.xerial.json.JSONObject;
import org.xerial.json.JSONUtil;
import org.xerial.json.JSONValue;
import org.xerial.json.JSONValueType;
import org.xerial.silk.impl.SilkDataLine;
import org.xerial.silk.impl.SilkFunction;
import org.xerial.silk.impl.SilkFunctionArg;
import org.xerial.silk.impl.SilkJSONValue;
import org.xerial.silk.impl.SilkNode;
import org.xerial.silk.impl.SilkNodeOccurrence;
import org.xerial.silk.impl.SilkValue;
import org.xerial.silk.plugin.SilkFunctionArgument;
import org.xerial.silk.plugin.SilkFunctionPlugin;
import org.xerial.util.ArrayDeque;
import org.xerial.util.FileResource;
import org.xerial.util.bean.TypeInformation;
import org.xerial.util.log.Logger;
import org.xerial.util.reflect.ReflectionUtil;
import org.xerial.util.tree.TreeEvent;
import org.xerial.util.tree.TreeStreamReader;
import org.xerial.util.xml.impl.TreeEventQueue;

/**
 * {@link TreeStreamReader} implementation for the Silk data format.
 * 
 * @author leo
 * 
 */
public class SilkStreamReader implements TreeStreamReader
{
    private static Logger _logger = Logger.getLogger(SilkStreamReader.class);

    private final SilkPullParser parser;
    private final SilkEnv parseContext;
    private TreeEventQueue eventQueue = new TreeEventQueue();
    private final ArrayDeque<TreeStreamReader> readerStack = new ArrayDeque<TreeStreamReader>();

    /**
     * Creates a new reader with the specified input stream
     * 
     * @param input
     *            `@throws IOException
     */
    protected SilkStreamReader(InputStream input) throws IOException
    {
        this(new InputStreamReader(input));
    }

    /**
     * Creates a new reader with the specified reader
     * 
     * @param input
     * @throws IOException
     */
    protected SilkStreamReader(Reader input) throws IOException
    {
        this(input, SilkEnv.newEnv());
    }

    /**
     * Creates a new reader inherited the given environment
     * 
     * @param input
     * @param env
     * @throws IOException
     */
    public SilkStreamReader(Reader input, SilkEnv env) throws IOException
    {
        this.parser = new SilkPullParser(input);
        this.parseContext = env;
    }

    /**
     * Concatenates the base path and the resource name
     * 
     * @param resourceBasePath
     * @param resourceName
     * @return
     */
    private static String getResourcePath(String resourceBasePath, String resourceName)
    {
        String resourcePath = resourceBasePath;
        if (!resourcePath.endsWith("/"))
            resourcePath += "/";
        resourcePath += resourceName;
        return resourcePath;
    }

    /**
     * Create a new reader for reading local resources
     * 
     * @param resourceBasePath
     * @param resourceName
     * @throws IOException
     */
    public SilkStreamReader(String resourceBasePath, String resourceName) throws IOException
    {
        this.parser = new SilkPullParser(new BufferedReader(new InputStreamReader(SilkWalker.class
                .getResourceAsStream(getResourcePath(resourceBasePath, resourceName)))));
        this.parseContext = SilkEnv.newEnv(resourceBasePath);
    }

    /**
     * Create a new reader for reading the specified resource URL
     * 
     * @param resourcePath
     * @throws IOException
     */
    public SilkStreamReader(URL resourcePath) throws IOException
    {
        this(resourcePath, SilkEnv.newEnv());
    }

    public SilkStreamReader(URL resource, SilkEnv env) throws IOException
    {
        String path = resource.toExternalForm();
        int fileNamePos = path.lastIndexOf("/");
        String resourceBasePath = fileNamePos > 0 ? path.substring(0, fileNamePos) : null;

        this.parser = new SilkPullParser(new BufferedReader(new InputStreamReader(resource.openStream())));
        this.parseContext = SilkEnv.newEnv(env, resourceBasePath);
    }

    public TreeEvent peekNext() throws XerialException
    {
        if (hasNext())
            return eventQueue.peekFirst();
        else
            return null;
    }

    public TreeEvent next() throws XerialException
    {
        if (hasNext())
            return getNextEvent();
        else
            return null;
    }

    /**
     * Enqueues a visit event.
     * 
     * @param nodeName
     * @param immediateNodeValue
     * @throws XerialException
     */
    private void visit(String nodeName, String immediateNodeValue) throws XerialException
    {
        eventQueue.push(TreeEvent.newVisitEvent(nodeName, immediateNodeValue));
    }

    /**
     * Enqueues a leave event
     * 
     * @param nodeName
     * @throws XerialException
     */
    private void leave(String nodeName) throws XerialException
    {
        eventQueue.push(TreeEvent.newLeaveEvent(nodeName));
    }

    /**
     * Enqueues a text event
     * 
     * @param textFragment
     * @throws XerialException
     */
    private void text(String textFragment) throws XerialException
    {
        eventQueue.push(TreeEvent.newTextEvent(parseContext.getContextNode().getName(), textFragment));
    }

    /**
     * Closed pre-opened contexts up to the specified indent level
     * 
     * @param newIndentLevel
     * @throws XerialException
     */
    private void closeContextUpTo(int newIndentLevel) throws XerialException
    {
        while (!parseContext.isContextNodeStackEmpty())
        {
            SilkContext context = parseContext.peekLastContext();
            SilkNode node = context.contextNode;
            if (node.getIndentLevel() >= newIndentLevel)
            {
                parseContext.popLastContext();

                if (parseContext.isAttributeOpen)
                {
                    // close attribute 
                    SilkNode attribute = node.getChildNodes().get(parseContext.contextNodeAttributeCursor);
                    leave(attribute.getName());
                    leave(node.getName());
                    parseContext.isAttributeOpen = false;
                }

                if (context.isOpen)
                {
                    // close context
                    String nodeName = node.getName();
                    leave(nodeName);
                }
            }
            else
                return;
        }
    }

    /**
     * Opens a new context for the given node
     * 
     * @param node
     *            new context node
     * @param visitor
     * @throws XerialException
     */
    private void openContext(SilkNode node) throws XerialException
    {
        int indentLevel = node.getIndentLevel();
        if (indentLevel != SilkNode.NO_INDENT)
            indentLevel += parseContext.getIndentationOffset();

        closeContextUpTo(indentLevel);
        openContext_internal(node);
    }

    private void openContext_internal(SilkNode node) throws XerialException
    {
        if (node.getName() == null)
        {
            // no name nodes must hierarchically organize attribute nodes
            for (SilkNode eachChild : node.getChildNodes())
            {
                eachChild.setNodeIndent(node.getNodeIndent());
                openContext_internal(eachChild);
            }
            return;
        }

        SilkContext currentContext = new SilkContext(node, true);
        parseContext.pushContext(currentContext);

        SilkNodeOccurrence occurrence = node.getOccurrence();
        if (occurrence.isSchemaOnlyNode())
        {
            currentContext.isOpen = false;
            // reset the attribute cursor
            parseContext.contextNodeAttributeCursor = 0;
            parseContext.isAttributeOpen = false;
            return; // do not invoke visit events
        }

        String nodeName = node.getName();
        SilkValue textValue = node.getValue();

        // process text values attached to the node
        if (textValue != null)
        {
            // When the text data is JSON, traverses the JSON data 
            if (textValue.isJSON())
            {

                SilkJSONValue jsonValue = SilkJSONValue.class.cast(textValue);
                if (jsonValue.isObject())
                {
                    visit(nodeName, null);
                    JSONObject jsonObj = new JSONObject(jsonValue.getValue());
                    walkJSONObject(jsonObj);
                }
                else
                {
                    currentContext.isOpen = false;
                    JSONArray jsonArray = new JSONArray(jsonValue.getValue());
                    walkJSONAray(jsonArray, nodeName);
                }
            }
            else if (textValue.isFunction())
            {
                // evaluate the function 
                visit(nodeName, null);
                SilkFunction function = SilkFunction.class.cast(textValue);
                evalFunction(function);

                return;
            }
            else
            {
                // Simple text value will be reported as it is.
                visit(nodeName, textValue.toString());
            }
        }
        else
        {
            if (occurrence == SilkNodeOccurrence.ZERO_OR_MORE)
            {
                // CSV data
                return; // do not invoke visit events
            }

            // Report a visit event without text value
            visit(nodeName, null);
        }

        // Traverse attribute nodes having text values. If no text value is specified for these attributes, 
        // they are schema elements for the following DATA_LINE. 
        for (SilkNode eachChild : node.getChildNodes())
        {
            if (eachChild.hasValue())
            {
                openContext(eachChild);
            }
        }

    }

    private static class FunctionReader implements TreeStreamReader
    {
        SilkFunctionPlugin plugin;

        public FunctionReader(SilkFunctionPlugin plugin)
        {
            this.plugin = plugin;
        }

        public TreeEvent peekNext() throws XerialException
        {
            return plugin.peekNext();
        }

        public TreeEvent next() throws XerialException
        {
            return plugin.next();
        }
    }

    /**
     * Evaluate the function
     * 
     * @param function
     * @throws XerialException
     */
    private void evalFunction(SilkFunction function) throws XerialException
    {
        SilkFunctionPlugin plugin = getPlugin(function.getName());
        if (plugin == null)
        {
            _logger.error(String.format("plugin %s not found", function.getName()));
            return;
        }
        // fill the function argument to the plugin instance
        populate(plugin, function.getArgumentList());

        // evaluate the function
        SilkEnv env = parseContext.newEnvFor(function);
        plugin.init(env);

        readerStack.addLast(new FunctionReader(plugin));
    }

    /**
     * Has finished reading the stream?
     */
    private boolean hasFinished = false;

    /**
     * Is next event available?
     * 
     * @return true if there are remaining events, otherwise fales
     * @throws XerialException
     */
    private boolean hasNext() throws XerialException
    {
        if (eventQueue.isEmpty())
        {
            if (hasFinished)
                return false;

            fillQueue();
            return hasNext();
        }
        else
            return true;
    }

    /**
     * Retrieves the next event from the queue. If the event queue is empty,
     * fill the queue with the next event
     * 
     * @return the next event.
     * @throws XerialException
     */
    private TreeEvent getNextEvent() throws XerialException
    {
        if (!eventQueue.isEmpty())
            return eventQueue.pop();

        if (hasFinished)
            throw new XerialError(XerialErrorCode.INVALID_STATE,
                    "hasNext() value must be checked before calling getNextEvent()");

        fillQueue();
        return getNextEvent();
    }

    private void walkMicroFormatRoot(SilkNode schemaNode, JSONArray value) throws XerialException
    {
        // e.g., exon(start, name)

        if (schemaNode.hasManyOccurrences())
        {
            // e.g., exon(start, name)*
            // multiple occurrences: [[start, end], [start, end], ... ] 
            for (int i = 0; i < value.size(); i++)
            {
                JSONArray eachElement = value.getJSONArray(i);
                if (eachElement == null)
                    continue;

                visit(schemaNode.getName(), null);
                int index = 0;
                for (SilkNode eachSubSchema : schemaNode.getChildNodes())
                {
                    walkMicroFormatElement(eachSubSchema, eachElement.get(index++));
                }
                leave(schemaNode.getName());
            }
        }
        else
        {
            // [e1, e2, ...]
            visit(schemaNode.getName(), null);
            int index = 0;
            if (schemaNode.getChildNodes().size() != value.size())
            {
                throw new XerialException(XerialErrorCode.INVALID_INPUT, String.format(
                        "data format doesn't match: schema=%s, value=%s", schemaNode, value));
            }
            for (SilkNode each : schemaNode.getChildNodes())
            {
                walkMicroFormatElement(each, value.get(index++));
            }
            leave(schemaNode.getName());
        }
    }

    private void walkMicroFormatElement(SilkNode schemaNode, JSONValue value) throws XerialException
    {
        if (schemaNode.hasChildren())
        {
            JSONArray array = value.getJSONArray();
            if (array != null)
                walkMicroFormatRoot(schemaNode, array);
            else
                throw new XerialException(XerialErrorCode.INVALID_INPUT, String.format(
                        "data format doesn't match: schema=%s, value=%s", schemaNode, value));
        }
        else
        {
            visit(schemaNode.getName(), value.toJSONString());
            leave(schemaNode.getName());
        }
    }

    private void evalColumnData(SilkNode node, String columnData) throws XerialException
    {
        try
        {
            if (node.hasChildren())
            {
                // micro-data format
                JSONArray array = new JSONArray(columnData);
                walkMicroFormatRoot(node, array);
                return;
            }

            String dataType = node.getDataType();
            if (dataType == null)
            {
                visit(node.getName(), columnData);
                leave(node.getName());
                return;
            }

            if (dataType.equalsIgnoreCase("json"))
            {
                JSONValue json = JSONUtil.parseJSON(columnData);
                if (json.getJSONObject() != null)
                {
                    if (node.getName().equals("_")) // no name object
                    {
                        walkJSONValue(json, node.getName());
                    }
                    else
                    {
                        visit(node.getName(), null);
                        walkJSONValue(json, node.getName());
                        leave(node.getName());
                    }
                }
                else
                    walkJSONValue(json, node.getName());
            }
            else
            {
                visit(node.getName(), columnData);
                leave(node.getName());
            }
        }
        catch (JSONException e)
        {
            throw new XerialException(e.getErrorCode(), String.format("line=%d: %s", parser.getLine(), e.getMessage()));
        }

    }

    /**
     * Fill the queue by retrieving the next event from the pull parser.
     * 
     * @throws XerialException
     */
    private void fillQueue() throws XerialException
    {
        if (!readerStack.isEmpty())
        {
            TreeEvent e = readerStack.peekLast().next();
            if (e == null)
            {
                readerStack.removeLast();
                fillQueue();
            }
            else
            {
                eventQueue.push(e);
                return;
            }
        }

        if (!parser.hasNext())
        {
            // no more input data
            closeContextUpTo(parseContext.getIndentationOffset());
            hasFinished = true;
            return;
        }

        SilkEvent currentEvent = parser.next();

        if (_logger.isDebugEnabled())
        {
            _logger.debug("stack: " + parseContext.getContextNodeStack());
            _logger.debug(currentEvent);
        }

        switch (currentEvent.getType())
        {
        case NODE:
            // push context node
            SilkNode newContextNode = SilkNode.class.cast(currentEvent.getElement());
            openContext(newContextNode);
            break;
        case FUNCTION:
            SilkFunction function = SilkFunction.class.cast(currentEvent.getElement());
            evalFunction(function);
            break;
        case DATA_LINE:
            // pop the context stack until finding a node for stream data node occurrence
            while (!parseContext.isContextNodeStackEmpty())
            {
                SilkContext context = parseContext.peekLastContext();
                SilkNode node = context.contextNode;
                if (!node.getOccurrence().isFollowedByStreamData())
                {
                    parseContext.popLastContext();
                    if (context.isOpen)
                        leave(node.getName());
                }
                else
                    break;
            }

            if (parseContext.isContextNodeStackEmpty())
            {
                // use default column names(c1, c2, ...) 
                SilkDataLine line = SilkDataLine.class.cast(currentEvent.getElement());
                String[] columns = line.getDataLine().trim().split("\t");
                int index = 1;
                visit("row", null);
                for (String each : columns)
                {
                    String columnName = String.format("c%d", index++);

                    // TODO use evalColumnData
                    visit(columnName, each);
                    leave(columnName);
                }
                leave("row");
            }
            else
            {
                SilkContext context = parseContext.peekLastContext();
                SilkNode schema = context.contextNode;
                SilkDataLine line = SilkDataLine.class.cast(currentEvent.getElement());
                switch (schema.getOccurrence())
                {
                case SEQUENCE:
                    text(line.getDataLine().trim());
                    break;
                case ZERO_OR_MORE:
                    // CSV data
                {
                    String[] csv = line.getDataLine().split(",");
                    String name = schema.getName();
                    for (String each : csv)
                    {
                        String value = each.trim();
                        evalColumnData(schema, value);
                    }
                }
                    break;
                case TABBED_SEQUENCE:
                {
                    String[] columns = line.getDataLine().trim().split("\t");
                    int columnIndex = 0;
                    visit(schema.getName(), schema.hasValue() ? schema.getValue().toString() : null);
                    for (int i = 0; i < schema.getChildNodes().size(); i++)
                    {
                        SilkNode child = schema.getChildNodes().get(i);
                        if (child.hasValue())
                        {
                            // output the default value for the column 
                            visit(child.getName(), child.getValue().toString());
                            leave(child.getName());
                        }
                        else
                        {
                            if (columnIndex < columns.length)
                            {
                                String columnData = columns[columnIndex++];
                                evalColumnData(child, columnData);
                            }
                        }
                    }
                    leave(schema.getName());
                    break;
                }
                case MULTILINE_SEQUENCE:
                {
                    int cursor = parseContext.contextNodeAttributeCursor;

                    if (cursor >= schema.getChildNodes().size())
                        break;

                    SilkNode attributeNode = schema.getChildNodes().get(cursor);
                    if (cursor == 0 && !parseContext.isAttributeOpen)
                    {
                        visit(schema.getName(), schema.hasValue() ? schema.getValue().toString() : null);
                    }
                    if (!parseContext.isAttributeOpen)
                    {
                        if (attributeNode.hasValue())
                            visit(attributeNode.getName(), attributeNode.getValue().toString());
                        else
                            visit(attributeNode.getName(), null);

                        parseContext.isAttributeOpen = true;
                    }
                    text(line.getDataLine().trim());
                    break;
                }
                }
            }
            break;
        case MULTILINE_ENTRY_SEPARATOR: // >>
        {
            SilkContext context = parseContext.peekLastContext();
            SilkNode schema = context.contextNode;
            if (parseContext.isAttributeOpen)
            {
                SilkNode attributeNode = schema.getChildNodes().get(parseContext.contextNodeAttributeCursor);
                leave(attributeNode.getName());
            }
            leave(schema.getName());
            // reset
            parseContext.contextNodeAttributeCursor = 0;
            parseContext.isAttributeOpen = false;
            break;
        }
        case MULTILINE_SEPARATOR: // --
        {
            SilkContext context = parseContext.peekLastContext();
            SilkNode schema = context.contextNode;
            if (parseContext.isAttributeOpen)
            {
                SilkNode attributeNode = schema.getChildNodes().get(parseContext.contextNodeAttributeCursor);
                leave(attributeNode.getName());
            }
            parseContext.contextNodeAttributeCursor++;
            parseContext.isAttributeOpen = false;
            break;
        }
        case BLANK_LINE:
            break;

        case PREAMBLE:
            break;

        }

    }

    private void walkJSONAray(JSONArray jsonArray, String parentNodeName) throws XerialException
    {
        for (JSONValue each : jsonArray)
        {
            walkJSONValue(each, parentNodeName);
        }
    }

    private void walkJSONObject(JSONObject jsonObj) throws XerialException
    {
        for (String key : jsonObj.keys())
        {
            JSONValue val = jsonObj.get(key);
            walkJSONValue(val, key);
        }
    }

    private void walkJSONValue(JSONValue value, String parentNodeName) throws XerialException
    {
        JSONValueType type = value.getValueType();
        switch (type)
        {
        case Array:
            walkJSONAray(value.getJSONArray(), parentNodeName);
            break;
        case Object:
            walkJSONObject(value.getJSONObject());
            break;
        case Boolean:
            visit(parentNodeName, value.toString());
            leave(parentNodeName);
            break;
        case Double:
            visit(parentNodeName, value.toString());
            leave(parentNodeName);
            break;
        case Integer:
            visit(parentNodeName, value.toString());
            leave(parentNodeName);
            break;
        case Null:
            visit(parentNodeName, value.toString());
            leave(parentNodeName);
            break;
        case String:
            visit(parentNodeName, value.toString());
            leave(parentNodeName);
            break;
        }

    }

    /**
     * Plugin holder
     */
    private static Map<String, Class<SilkFunctionPlugin>> pluginTable = null;

    /**
     * Get the plugin of the specified name
     * 
     * @param name
     *            plugin name
     * @return plugin instance or null if no corresponding plugin is found.
     */
    private SilkFunctionPlugin getPlugin(String name)
    {
        Class<SilkFunctionPlugin> pluginClass = getPluginTable().get(name);
        if (pluginClass == null)
            return null;

        try
        {
            SilkFunctionPlugin pluginInstance = pluginClass.newInstance();
            return pluginInstance;
        }
        catch (InstantiationException e)
        {
            _logger.warn(e);
            return null;
        }
        catch (IllegalAccessException e)
        {
            _logger.warn(e);
            return null;
        }
    }

    private Map<String, Class<SilkFunctionPlugin>> getPluginTable()
    {
        if (pluginTable == null)
        {
            pluginTable = new TreeMap<String, Class<SilkFunctionPlugin>>();
            // load plugins 
            for (Class<SilkFunctionPlugin> each : FileResource.findClasses(SilkFunctionPlugin.class.getPackage(),
                    SilkFunctionPlugin.class, SilkWalker.class.getClassLoader()))
            {
                String functionName = each.getSimpleName().toLowerCase();
                _logger.trace("loaded " + functionName);
                pluginTable.put(functionName, each);
            }
        }

        return pluginTable;
    }

    /**
     * Information of the function (plugin) arguments (
     * {@link SilkFunctionArgument}) described in the Class definition, which
     * implements {@link SilkFunctionPlugin}.
     * 
     * @author leo
     * 
     */
    private static class PluginField
    {
        Field field;
        SilkFunctionArgument argInfo;

        private PluginField(SilkFunctionArgument argInfo, Field field)
        {
            this.argInfo = argInfo;
            this.field = field;
        }
    }

    private static class PluginHolder
    {
        Class< ? extends SilkFunctionPlugin> pluginClass;
        ArrayList<PluginField> argumentFieldList = new ArrayList<PluginField>();
        Map<String, PluginField> keyValueFieldTable = new HashMap<String, PluginField>();

        public PluginHolder(Class< ? extends SilkFunctionPlugin> pluginClass)
        {
            this.pluginClass = pluginClass;

            //ArrayList<SilkFunctionArgument> argDefs = new ArrayList<SilkFunctionArgument>();
            for (Field eachField : pluginClass.getDeclaredFields())
            {
                SilkFunctionArgument argInfo = eachField.getAnnotation(SilkFunctionArgument.class);
                if (argInfo != null)
                {
                    PluginField pf = new PluginField(argInfo, eachField);
                    if (argInfo.name().equals(SilkFunctionArgument.NO_VALUE))
                        argumentFieldList.add(pf);
                    else
                        keyValueFieldTable.put(argInfo.name(), pf);
                }
            }

            // sort arguments in the order of their ordinal
            Collections.sort(argumentFieldList, new Comparator<PluginField>() {
                public int compare(PluginField o1, PluginField o2)
                {
                    return o1.argInfo.ordinal() - o2.argInfo.ordinal();
                }
            });

        }

        /**
         * Bind function arguments to the plug-in instance
         * 
         * @param plugin
         *            the instance of the plug-in
         * @param args
         *            the function arguments
         */
        public void populate(SilkFunctionPlugin plugin, List<SilkFunctionArg> args)
        {
            int noNameArgCount = 0;
            for (SilkFunctionArg eachArgument : args)
            {
                String argValue = eachArgument.getValue().toString();
                try
                {
                    if (eachArgument.hasName())
                    {
                        // key value arg
                        PluginField f = keyValueFieldTable.get(eachArgument.getName());
                        if (f == null)
                        {
                            _logger.warn("unknown argument: " + eachArgument);
                            continue;
                        }
                        ReflectionUtil.setFieldValue(plugin, f.field, argValue);
                    }
                    else
                    {
                        // unnamed argument
                        // matching argument order
                        if (noNameArgCount >= argumentFieldList.size())
                        {
                            _logger.warn(String.format("no corresponding field for the argument %s is found",
                                    eachArgument));
                            continue;
                        }
                        PluginField f = argumentFieldList.get(noNameArgCount);
                        ReflectionUtil.setFieldValue(plugin, f.field, argValue);

                        if (!TypeInformation.isCollection(f.field.getType()))
                            noNameArgCount++;
                    }
                }
                catch (XerialException e)
                {
                    _logger.error(e);
                }

            }
        }

    }

    /**
     * Fill the plug-in argument fields with the given arguments
     * 
     * @param plugin
     *            plug-in instance.
     * @param args
     *            function arguments.
     */
    private static void populate(SilkFunctionPlugin plugin, List<SilkFunctionArg> args)
    {
        PluginHolder holder = new PluginHolder(plugin.getClass());
        holder.populate(plugin, args);
    }

}
