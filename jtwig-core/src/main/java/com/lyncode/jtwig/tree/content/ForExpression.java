/**
 * Copyright 2012 Lyncode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lyncode.jtwig.tree.content;

import com.lyncode.jtwig.JtwigContext;
import com.lyncode.jtwig.exception.CalculateException;
import com.lyncode.jtwig.exception.ComposeException;
import com.lyncode.jtwig.exception.RenderException;
import com.lyncode.jtwig.resource.JtwigResource;
import com.lyncode.jtwig.tree.api.Composable;
import com.lyncode.jtwig.tree.api.Renderable;
import com.lyncode.jtwig.tree.structural.BlockExpression;
import com.lyncode.jtwig.tree.value.FunctionElement;
import com.lyncode.jtwig.tree.value.Variable;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ForExpression implements Renderable, Composable<ForExpression> {
    private Variable item;
    private Object list;
    private List<FunctionElement> filters = new ArrayList<FunctionElement>();
    private Content content;

    public ForExpression (Variable item, Object list) {
        this.item = item;
        this.list = list;
    }

    public boolean add (FunctionElement function) {
        filters.add(function);
        return true;
    }

    public Variable getItem() {
        return item;
    }

    public Object getList() {
        return list;
    }

    public List<FunctionElement> getFilters() {
        return filters;
    }

    public Content getContent() {
        return content;
    }

    public boolean setContent(Content content) {
        this.content = content;
        return true;
    }

    @Override
    public boolean render(OutputStream outputStream, JtwigContext context) throws RenderException {
        try {
            ObjectIterator iterator = new ObjectIterator(context.resolve(list));
            while (iterator.hasNext()) {
                Object object = iterator.next();
                context.set(item.getIdentifier(), object);
                content.render(outputStream, context);
            }
            return true;
        } catch (CalculateException e) {
            throw new RenderException(e);
        }
    }

    @Override
    public ForExpression compose(JtwigResource resource) throws ComposeException {
        content = content.compose(resource);
        return this;
    }

    @Override
    public boolean replace(BlockExpression expression) throws ComposeException {
        return content.replace(expression);
    }

    public static class ObjectIterator {
        private Iterator<Object> iterator = null;

        public ObjectIterator(Object context) {
            List<Object> list = new ArrayList<Object>();
            if (context instanceof Iterable) {
                Iterator iterator = ((Iterable) context).iterator();
                while (iterator.hasNext())
                    list.add(iterator.next());
            } else if (context.getClass().isArray()) {
                Object[] objects = (Object[]) context;
                for (Object obj : objects)
                    list.add(obj);
            } else list.add(context);

            iterator = list.iterator();
        }

        public boolean hasNext () {
            return iterator.hasNext();
        }

        public Object next () {
            return iterator.next();
        }
    }
}