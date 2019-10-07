/*
 * This file is a part of BSL Language Server.
 *
 * Copyright © 2018-2019
 * Alexey Sosnoviy <labotamy@gmail.com>, Nikita Gryzlov <nixel2007@gmail.com> and contributors
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * BSL Language Server is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * BSL Language Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BSL Language Server.
 */
package com.github._1c_syntax.bsl.languageserver.diagnostics;

import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticMetadata;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticScope;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticSeverity;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticType;
import com.github._1c_syntax.bsl.parser.BSLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.annotation.CheckForNull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DiagnosticMetadata(
	type = DiagnosticType.ERROR,
	severity = DiagnosticSeverity.CRITICAL,
	minutesToFix = 30,
	scope = DiagnosticScope.BSL
)
public class UsingObjectNotAvailableUnixDiagnostic extends AbstractVisitorDiagnostic {

	private static final Pattern patternNewExpression = Pattern.compile(
		"^(COMОбъект|COMObject|Почта|Mail)",
		Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private static final Pattern patternTypePlatform = Pattern.compile(
		"Linux_x86",
		Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	/**
	 * Проверяем все объявления на тип COMОбъект или Почта. Если условие выше (обрабатывается вся
	 * цепочка) с проверкой ТипПлатформы = Linux не найдено в методе, то диагностика срабатывает.
	 * Пример:
	 * Компонента = Новый COMОбъект("System.Text.UTF8Encoding");
	 */
	@Override
	public ParseTree visitNewExpression(BSLParser.NewExpressionContext ctx) {
		BSLParser.TypeNameContext typeNameContext = ctx.typeName();
		if (typeNameContext == null) {
			return super.visitNewExpression(ctx);
		}
		Matcher matcherTypeName = patternNewExpression.matcher(typeNameContext.getText());
		if (matcherTypeName.find()) {
			// ищем условие выше, пока не дойдем до null
			if (!isFindIfBranchWithLinuxCondition(ctx)) {
				diagnosticStorage.addDiagnostic(ctx, getDiagnosticMessage(typeNameContext.getText()));
			}
		}
		return super.visitNewExpression(ctx);
	}

	private boolean isFindIfBranchWithLinuxCondition(ParserRuleContext element) {
		ParserRuleContext ancestor = getAncestorByRuleIndex(element, BSLParser.RULE_ifBranch);
		if (ancestor == null) {
			return false;
		}
		String content = ancestor.getText();
		Matcher matcher = patternTypePlatform.matcher(content);
		if (!matcher.find()) {
			return false;
		}
		return isFindIfBranchWithLinuxCondition(ancestor);
	}

	// TODO: должно быть в Tree
	@CheckForNull
	private static ParserRuleContext getAncestorByRuleIndex(ParserRuleContext element, int type) {
		ParserRuleContext parent = element.getParent();
		if (parent == null) {
			return null;
		}
		if (parent.getRuleIndex() == type) {
			return parent;
		}
		return getAncestorByRuleIndex(parent, type);
	}

}
