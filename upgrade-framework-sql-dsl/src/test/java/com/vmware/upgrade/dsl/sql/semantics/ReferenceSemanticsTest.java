/* ****************************************************************************
 * Copyright (c) 2012-2014 VMware, Inc. All Rights Reserved.
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

package com.vmware.upgrade.dsl.sql.semantics;

import java.util.HashMap;

import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.dsl.model.UpgradeDefinitionModel;
import com.vmware.upgrade.dsl.sql.syntax.SyntaxTestUtil;
import com.vmware.upgrade.dsl.sql.util.SQLStatementFactory;
import com.vmware.upgrade.dsl.syntax.UnknownKeywordException;
import com.vmware.upgrade.sql.SQLStatement;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ReferenceSemanticsTest {
    @SuppressWarnings("serial")
    @DataProvider(name = "reference-statements")
    public Object[][] referenceStatements() {
        return new Object[][] {
                new Object[] {
                        "reference 'p_id' of 'person' from 'person_id' of 'virtual_machine_library'",
                        SQLStatementFactory.create(
                                "ALTER TABLE virtual_machine_library ADD CONSTRAINT fk_virt_mach_lib2person " +
                                "FOREIGN KEY ( person_id ) REFERENCES person ( p_id )"
                        )
                },
                new Object[] {
                        "reference 'c' and 'd' of 't' from 'c2' and 'd2' of 't2'",
                        SQLStatementFactory.create(
                                "ALTER TABLE t2 ADD CONSTRAINT fk_t22t FOREIGN KEY ( c2, d2 ) REFERENCES t ( c, d )"
                        )
                },
                new Object[] {
                        "reference 'c' and 'd' of 't' from 'c2' and 'd2' of 't2' on_delete cascade",
                        SQLStatementFactory.create(
                                "ALTER TABLE t2 ADD CONSTRAINT fk_t22t FOREIGN KEY ( c2, d2 ) REFERENCES t ( c, d ) ON DELETE CASCADE"
                        )
                },
                new Object[] {
                        "reference 'c' and 'd' of 't' from 'c2' and 'd2' of 't2' on_delete default: cascade, oracle: set_null",
                        SQLStatementFactory.create(
                                new HashMap<String, String>() {{
                                    put("oracle", "ALTER TABLE t2 ADD CONSTRAINT fk_t22t FOREIGN KEY ( c2, d2 ) REFERENCES t ( c, d ) ON DELETE SET NULL");
                                    put("ms_sql", "ALTER TABLE t2 ADD CONSTRAINT fk_t22t FOREIGN KEY ( c2, d2 ) REFERENCES t ( c, d ) ON DELETE CASCADE");
                                    put("postgres", "ALTER TABLE t2 ADD CONSTRAINT fk_t22t FOREIGN KEY ( c2, d2 ) REFERENCES t ( c, d ) ON DELETE CASCADE");
                                }}
                        )
                },
                new Object[] {
                        "reference 'c' and 'd' of 't' from 'c2' and 'd2' of 't2' on_delete no_action",
                        SQLStatementFactory.create(
                                "ALTER TABLE t2 ADD CONSTRAINT fk_t22t FOREIGN KEY ( c2, d2 ) REFERENCES t ( c, d ) ON DELETE NO ACTION"
                        )
                },
                new Object[] {
                        "unreference 'person' from 'virtual_machine_library'",
                        SQLStatementFactory.create(
                                "ALTER TABLE virtual_machine_library DROP CONSTRAINT fk_virt_mach_lib2person"
                        )
                }
        };

    }

    @DataProvider(name = "invalid-reference-statements")
    public Object[][] invalidReferenceStatements() {
        return new Object[][] {
                new Object[] {
                        "reference 'c' and 'd' of 't' from 'c2' and 'd2' of 't2' on_delete",
                        UnknownKeywordException.class,
                        "Missing keyword following 'on_delete'"
                }
        };
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "reference-statements")
    public void compareReferenceStatements(String ddl, SQLStatement expected) {
        final UpgradeDefinitionModel upgrade = SemanticTestUtil.createUpgradeModel(ddl);

        SemanticTestUtil.compareModelToSQL(upgrade, expected);
    }

    @Test(groups = { TestGroups.UNIT }, dataProvider = "invalid-reference-statements")
    public void compareInvalidReferenceStatements(String ddl, Class<?> clazz, String expectedMsg) {
        SyntaxTestUtil.verifyException(ddl, clazz, expectedMsg);
    }
}
