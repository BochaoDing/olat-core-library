
function fromOlatTimestamp(timestamp) {
    return new Date(timestamp).toISOString();
}

function jsonGet(endPoint, params, onSuccess, onFailure, onFinally) {
    var request = {
        url: "http://localhost:8080/restapi/" + endPoint,
        dataType: "json",
        type: "GET",
        data: params
    };
    jsonRequest(request, onSuccess, onFailure, onFinally);
}

function jsonPost(endPoint, params, onSuccess, onFailure, onFinally) {
    var request = {
        url: "http://localhost:8080/restapi/" + endPoint,
        dataType: "json",
        type: "POST",
        data: params
    };
    jsonRequest(request, onSuccess, onFailure, onFinally);
}

function jsonRequest(request, onSuccess, onFailure, onFinally) {
    var jqxhr = jQuery.ajax(request);
    if (onSuccess) {
        jqxhr.done(function (data) {
            onSuccess(data);
        });
    }
    if (onFailure) {
        jqxhr.fail(function (data) {
            onFailure(data);
        });
    }
    if (onFinally) {
        jqxhr.always(function (data) {
            onFinally(data);
        });
    }
}

function renderElement(elementName, props, mountingPoint) {
    console.log('deferring rendering element with 200ms to give time for DOM to ensure the mounting point');
    setTimeout(function () {
        ReactDOM.render(React.createElement(elementName, props), document.getElementById(mountingPoint));
    }, 200);
}

class OLATComponent extends React.Component {
    constructor(props) {
        super(props);
        var language = props.language || this.getDefaultLanguage();
        this.state = {
            language: language,
            i18n: this.getTranslations(language)
        };
    }

    onEvent(cmd, params) {
        console.log('OLATComponent.onEvent()', cmd, params);
        sendMessage(cmd, params);
    }
    sendMessage(cmd, params) {
        console.log('OLATComponent.sendMessage()', cmd, params);
        // return o_XHREvent('/auth/1:1:1000000436:1:1/',true,true,'cid','create.new.meeting');
    }
    getTranslations(language) {
        return {};
    }
    getDefaultLanguage() {
        return 'sessionLanguage' in localStorage ? localStorage.getItem('sessionLanguage') : 'en';
    }
    translate(key) {
        var translations = this.state.i18n;
        //console.log('translate', key, ((key in translations) ? translations[key] : key));
        return key in translations ? translations[key] : key;
    }
}

class OLATSortableTable extends OLATComponent {
    constructor(props) {
        super(props);
        jQuery.extend(this.state, {
            list: this.props.list || [],
            sortColumn: this.props.sortColumn || "",
            sortAsc: 'sortAsc' in this.props ? this.props.sortAsc : true
        });
        this.sort = this.sort.bind(this);
        this.compare = this.compare.bind(this);
    }
    render() {
        var vm = this;
        var columns = this.props.columns || [];
        var list = this.state.list || [];
        return React.createElement(
            "table",
            { className: "table table-condensed table-striped table-hover" },
            React.createElement(
                "thead",
                null,
                React.createElement(
                    "tr",
                    null,
                    columns.map(function (columnInfo, index) {
                        var columnClasses = [];
                        if (columnInfo.sortable) {
                            columnClasses.push('o_orderby');
                            if (columnInfo.name === vm.state.sortColumn) {
                                columnClasses.push(vm.state.sortAsc ? 'o_orderby_asc' : 'o_orderby_desc');
                            }
                            return React.createElement(
                                "th",
                                { key: index },
                                React.createElement(
                                    "a",
                                    { className: columnClasses.join(' '), href: "javascript:", onClick: () => {
                                        vm.sort(columnInfo.name);
                                    } },
                                    columnInfo.label
                                )
                            );
                        } else {
                            return React.createElement(
                                "th",
                                { key: index },
                                columnInfo.label
                            );
                        }
                    })
                )
            ),
            React.createElement(
                "tbody",
                null,
                list.map(item => {
                    return this.renderRow(item, columns);
                })
            )
        );
    }

    renderRow(item, columns) {
        console.log('OLATSortableTable.renderRow()', item, columns);
        return React.createElement(
            "tr",
            null,
            columns.map(function (columnInfo, index) {
                return React.createElement(
                    "td",
                    { key: index },
                    columnInfo.name in item ? item[columnInfo.name] : ''
                );
            })
        );
    }
    sort(columnName) {
        const vm = this;
        let ascending = this.state.sortColumn === columnName ? !this.state.sortAsc : true;
        let list = this.props.list || [];
        console.log('OLATSortableTable.sort(' + columnName + ')');
        list.sort(function (a, b) {
            return ascending ? vm.compare(a, b, columnName) : vm.compare(b, a, columnName);
        });
        this.setState({ list: list, sortColumn: columnName, sortAsc: ascending });
    }
    compare(a, b, columnName) {
        if (columnName in a && columnName in b) {
            return a[columnName] < b[columnName] ? -1 : (a[columnName] === b[columnName] ? 0 : 1);
        } else {
            return 0; // when no comparison possible, keep current order
        }
    }
}

class SwitchMeetings extends OLATSortableTable {
    constructor(props) {
        console.log('SwitchMeetings.constructor()', props);
        super(props);
        this.openMeeting = this.openMeeting.bind(this);
        this.editMeeting = this.editMeeting.bind(this);
        this.deleteMeeting = this.deleteMeeting.bind(this);
    }

    openMeeting(meetingInfo) {
        super.sendMessage('open', meetingInfo);
    }

    editMeeting(meetingInfo) {
        super.sendMessage('edit', meetingInfo);
    }

    deleteMeeting(meetingInfo) {
        super.sendMessage('delete', meetingInfo);
    }
}

class PastMeetings extends SwitchMeetings {
    renderRow(meetingInfo, columns) {
        console.log('PastMeetings.renderItem()', meetingInfo, columns);
        return React.createElement(
            "tr",
            { key: meetingInfo.scoId },
            React.createElement(
                "td",
                null,
                React.createElement(
                    "a",
                    { href: "javascript:", onClick: () => this.openMeeting(meetingInfo) },
                    meetingInfo.name
                )
            ),
            React.createElement(
                "td",
                null,
                fromOlatTimestamp(meetingInfo.start)
            ),
            React.createElement(
                "td",
                null,
                fromOlatTimestamp(meetingInfo.end)
            ),
            React.createElement(
                "td",
                null,
                meetingInfo.materials
            ),
            React.createElement(
                "td",
                null,
                React.createElement(
                    "a",
                    { href: "javascript:", onClick: () => this.editMeeting(meetingInfo) },
                    "Edit"
                )
            ),
            React.createElement(
                "td",
                null,
                React.createElement(
                    "a",
                    { href: "javascript:", onClick: () => this.deleteMeeting(meetingInfo) },
                    "Delete"
                )
            )
        );
    }
}

class FutureMeetings extends SwitchMeetings {
    renderRow(meetingInfo, columns) {
        console.log('FutureMeetings.renderItem()', meetingInfo, columns);
        return React.createElement(
            "tr",
            { key: meetingInfo.scoId },
            React.createElement(
                "td",
                null,
                React.createElement(
                    "a",
                    { href: "javascript:", onClick: () => this.openMeeting(meetingInfo) },
                    meetingInfo.name
                )
            ),
            React.createElement(
                "td",
                null,
                fromOlatTimestamp(meetingInfo.start)
            ),
            React.createElement(
                "td",
                null,
                fromOlatTimestamp(meetingInfo.end)
            ),
            React.createElement(
                "td",
                null,
                meetingInfo.materials
            ),
            React.createElement(
                "td",
                null,
                React.createElement(
                    "a",
                    { href: "javascript:", onClick: () => this.openMeeting(meetingInfo) },
                    "Edit"
                )
            ),
            React.createElement(
                "td",
                null,
                React.createElement(
                    "a",
                    { href: "javascript:", onClick: () => this.deleteMeeting(meetingInfo) },
                    "Delete"
                )
            )
        );
    }
}

class SwitchInteractSelectMeeting extends OLATComponent {
    constructor(props) {
        console.log('SwitchInteractSelectMeeting.constructor()', props);
        super(props);
        jQuery.extend(this.state, {
            loading: true,
            meetings: []
        });
    }
    componentDidMount() {
        const vm = this;
        jsonGet('swin/' + this.props.courseId + '/' + this.props.nodeId + '/meetings', {}, function (data) {
            vm.setState({ meetings: data, loading: false });
        }, function (data) {}, function (data) {
            if (vm.state.loading) {
                vm.setState({ loading: false });
            }
        });
    }
    render() {
        console.log('SwitchInteractSelectMeeting.render', this.props, this.state);
        let pastMeetingsList = [];
        let futureMeetingsList = [];
        let meetings = this.state.meetings || [];
        const currentTime = new Date().getTime();
        meetings.map(function (meetingInfo, index) {
            if (meetingInfo.end < currentTime) {
                pastMeetingsList.push(meetingInfo);
            } else {
                futureMeetingsList.push(meetingInfo);
            }
        });
        const meetingsListColumns = [{ "name": "name", "label": this.translate('name'), "sortable": true }, { "name": "begin", "label": this.translate('begin'), "sortable": true }, { "name": "end", "label": this.translate('end'), "sortable": false }, { "name": "materials", "label": this.translate('materials'), "sortable": false }, { "name": "edit", "label": this.translate('edit'), "sortable": false }, { "name": "delete", "label": this.translate('delete'), "sortable": false }];
        let futureMeetings = futureMeetingsList.length > 0 ? React.createElement(FutureMeetings, { columns: meetingsListColumns, list: futureMeetingsList }) : React.createElement(
            "p",
            null,
            this.translate('no future meetings')
        );
        let pastMeetings = pastMeetingsList.length > 0 ? React.createElement(PastMeetings, { columns: meetingsListColumns, list: pastMeetingsList }) : React.createElement(
            "p",
            null,
            this.translate('no past meetings')
        );

        return React.createElement(
            "div",
            { className: "o_swin_meeting_selection" },
            futureMeetings,
            pastMeetings
        );
    }

    getTranslations(language) {
        switch (language) {
            case 'en':
            default:
                return {
                    "name": "Name",
                    "begin": "Begin",
                    "end": "End",
                    "materials": "Materials",
                    "edit": "Edit",
                    "delete": "Delete",

                    "no past meetings": "There are no future meetings in this course yet",
                    "no future meetings": "There are no future meetings in this course yet"
                };
        }
    }
}