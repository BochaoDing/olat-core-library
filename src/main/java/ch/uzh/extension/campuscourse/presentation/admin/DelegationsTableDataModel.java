package ch.uzh.extension.campuscourse.presentation.admin;

import org.olat.admin.securitygroup.gui.GroupMemberView;
import org.olat.admin.securitygroup.gui.IdentitiesOfGroupTableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DelegationsTableDataModel extends IdentitiesOfGroupTableDataModel {

    DelegationsTableDataModel(List<GroupMemberView> combo, Locale locale, List<UserPropertyHandler> userPropertyHandlers, boolean isAdministrativeUser) {
        super(combo, locale, userPropertyHandlers, isAdministrativeUser);
    }

    @Override
    public final Object getValueAt(int row, int col) {

        GroupMemberView groupMemberView = getObject(row);
        Identity identity = groupMemberView.getIdentity();
        List<UserPropertyHandler> userPropertyHandlers = getUserPropertyHandlers();

        Date addedAt = groupMemberView.getAddedAt();
        User user = identity.getUser();

        if (col == 0) {
            return identity.getName();
        } else if (col > 0 && col < userPropertyHandlers.size() + 1) {
            // get user property for this column
            UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col - 1);
            String value = userPropertyHandler.getUserProperty(user, getLocale());
            return (value == null ? "n/a" : value);
        } else if (col == userPropertyHandlers.size() + 1) {
            return addedAt;
        } else {
            return "error";
        }
    }
}
