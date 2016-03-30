/* ****************************************************************************
 * Copyright (c) 2012-2016 VMware, Inc. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * ****************************************************************************/

package com.vmware.upgrade.dsl.sql.model


/**
 * {@link CommentModel} is an {@link SQLStatement} that represents a table or
 * column comment.
 *
 * @author Ryan Lewis ryanlewis@vmware.com
 * @version 1.0
 * @since 1.0
 */
public interface CommentModel extends TransformingModel {
    /**
     * Sets the text of the comment.
     *
     * @param text the text of the comment.
     */
    public void setText(text)

    /**
     * Sets the name of entity being commented on. This will be either the name
     * of a table or the name of a column.
     *
     * @param name table or column name
     */
    public void setEntity(name)

    /**
     * In the case that a column name was given by {@link #setEntity(String)},
     * the name of the table it's in is set-able by this method.
     *
     * @param tableName
     */
    public void columnComment(tableName)
}
