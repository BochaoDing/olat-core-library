package ch.uzh.campus.presentation;

import org.olat.admin.securitygroup.gui.GroupMemberView;
import org.olat.admin.securitygroup.gui.IdentitiesOfGroupTableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DelegationIdentitiesOfGroupTableDataModel extends IdentitiesOfGroupTableDataModel {

    public DelegationIdentitiesOfGroupTableDataModel(List<GroupMemberView> combo, Locale locale, List<UserPropertyHandler> userPropertyHandlers, boolean isAdministrativeUser) {
        super(combo, locale, userPropertyHandlers, isAdministrativeUser);
    }

    @Override
    public final Object getValueAt(final int row, final int col) {

        GroupMemberView co = getObject(row);
        final Identity identity = co.getIdentity();
        final List<UserPropertyHandler> userPropertyHandlers = getUserPropertyHandlers();

        final Date addedTo = co.getAddedAt();
        final User user = identity.getUser();

        if (col == 0) {
            return identity.getName();
        } else if (col > 0 && col < userPropertyHandlers.size() + 1) {
            // get user property for this column
            final UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col - 1);
            final String value = userPropertyHandler.getUserProperty(user, getLocale());
            return (value == null ? "n/a" : value);
        } else if (col == userPropertyHandlers.size() + 1) {
            return addedTo;
        } else {
            return "error";
        }
    }
}
