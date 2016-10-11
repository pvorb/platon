var infoKey = 'platonUserInfo';
var rememberUserKey = 'platonRememberUserKey';
module.exports = {
    getUserInfo: function getUserInfo() {
        var userInfo = localStorage.getItem(infoKey);
        if (userInfo === null) {
            return {};
        } else {
            return JSON.parse(userInfo)
        }
    },
    storeUserInfo: function storeUserInfo(userInfo) {
        localStorage.setItem(infoKey, JSON.stringify(userInfo));
        localStorage.setItem(rememberUserKey, true);
    },
    removeUserInfo: function removeUserInfo() {
        localStorage.removeItem(infoKey);
        localStorage.setItem(rememberUserKey, false);
    },
    getRememberUser: function getRememberUser() {
        var rememberUser = localStorage.getItem(rememberUserKey);
        return rememberUser === null || rememberUser === 'true';
    }
};
