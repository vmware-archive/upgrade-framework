/* ****************************************************************************
 * Copyright (c) 2011-2014 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.dsl.syntax

import com.vmware.upgrade.dsl.model.ManifestModel
import com.vmware.upgrade.dsl.model.UpgradeTaskModel
import com.vmware.upgrade.sequencing.Version

/**
 * Syntax to parse a manifest object describing zero or more {@link UpgradeTaskModel}s.
 *
 * @author Emil Sit <sit@vmware.com>
 * @version 1.0
 * @since 1.0
 */
class ManifestSyntax {
    ManifestModel manifest = new ManifestModel()

    def name(manifestName) {
        manifest.name = manifestName
    }

    def from(version) {
        [see: { subManifest ->
            manifest.addAll(subManifest.upgrades)
         },
         call: { upgrade ->
             addUpgrade(new Version(version), new Version(version).getNext(), upgrade)
         },
         to: { target ->
             [call: { upgrade ->
                  addUpgrade(new Version(version), new Version(target), upgrade)
              }
             ]
         }
        ]
    }

    def to(version) {
        [call: { upgrade ->
            final Version toVersion =  new Version("${version}")

            for (int i = 0; new Version("${i}") < toVersion; i++) {
                addUpgrade(new Version("${i}"), toVersion, upgrade)
            }
        }]
    }

    def addUpgrade(source, target, definition) {
        if (manifest.getUpgrade(source) != null) {
            throw new DuplicateSourceException(source)
        }
        UpgradeTaskModel taskModel = [definition: definition, source: source, target: target] as UpgradeTaskModel
        manifest.addUpgrade(taskModel)
    }
}
