/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.dao;

import java.util.List;

import org.cerberus.entity.User;
import org.cerberus.exception.CerberusException;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
public interface IUserDAO {

    /**
     * @param login
     * @return the user that correspond to the login.
     * @throws CerberusException in case the user is not found.
     */
    User findUserByKey(String login) throws CerberusException;

    /**
     * @return a list of all users.
     * @throws CerberusException in case no user can be found.
     */
    List<User> findAllUser() throws CerberusException;

    /**
     * Insert user into the database.
     *
     * @param user
     * @return the inserted user
     * @throws CerberusException if we did not manage to insert the user.
     */
    public boolean insertUser(User user);

    /**
     * delete user from the database.
     *
     * @param user
     * @throws CerberusException if user could not be removed.
     */
    public boolean deleteUser(User user);

    /**
     * update user that correspond to the user.getUserID.
     *
     * @param user
     * @return the updated user
     * @throws CerberusException if the user could not be updated.
     */
    public boolean updateUser(User user);

    /**
     * @param user
     * @param password as the new value of the password.
     * @return the user updated with the new password.
     * @throws CerberusException if the password cannot be updated.
     */
    public User updateUserPassword(User user, String password) throws CerberusException;

    /**
     * @param user
     * @param password
     * @return true if password match and false if password does not match.
     */
    boolean verifyPassword(User user, String password);
    
        /**
     *
     * @param start first row of the resultSet
     * @param amount number of row of the resultSet
     * @param column order the resultSet by this column
     * @param dir Asc or desc, information for the order by command
     * @param searchTerm search term on all the column of the resultSet
     * @param individualSearch search term on a dedicated column of the
     * resultSet
     * @return
     */
    List<User> findTestDataListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);
    
    /**
     * 
     * @param searchTerm words to be searched in every column (Exemple : article)
     * @param inds part of the script to add to where clause (Exemple : `type` = 'Article')
     * @return The number of records for these criterias
     */
    Integer getNumberOfUserPerCriteria(String searchTerm, String inds);

    /**
     *
     * @param system
     * @return
     */
    List<User> findAllUserBySystem(String system);
}
