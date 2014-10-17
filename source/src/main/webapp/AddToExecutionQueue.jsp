<%--
  ~ Cerberus  Copyright (C) 2013  vertigo17
  ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  ~
  ~ This file is part of Cerberus.
  ~
  ~ Cerberus is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ Cerberus is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="include/function.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<form name="AddToExecutionQueue" action="AddToExecutionQueue"
	method="POST">
	<table>
		<tr>
			<td>SelectedTest</td>
			<td><input type="text" name="SelectedTest"
				value="Test=3DSecure&TestCase=0001A"></td>
		</tr>
		<tr>
			<td>Country</td>
			<td><input type="text" name="Country" value="FR"></td>
		</tr>
		<tr>
			<td>Environment</td>
			<td><input type="text" name="Environment" value="QA"></td>
		</tr>
		<tr>
            <td>Browser</td>
            <td><input type="text" name="Browser" value="chrome"></td>
        </tr>
		<tr>
			<td>RobotIP</td>
			<td><input type="text" name="RobotIP" value="127.0.0.1"></td>
		</tr>
		<tr>
			<td>RobotPort</td>
			<td><input type="text" name="RobotPort" value="5555"></td>
		</tr>
		<tr>
			<td>Tag</td>
			<td><input type="text" name="Tag" value="TAG"></td>
		</tr>
	</table>
	<input id="AddToExecutionQueue" type="submit" value="Send Query" />

</form>