var teamsService = angular.module('pp.service.teams', ['pp.service.rest'], function () {
});

teamsService.constant('teamsConstants', {
    resource: 'teams'
});

teamsService.factory('teamsService', ['$q', 'restService', 'teamsConstants', function ($q, restService, teamsConstants) {

    function Team(name) {
        this.name = name;
        this.count = '-';
    };

    return {
        getTeams: function () {

            var deferred = $q.defer();

            restService.findAll(teamsConstants.resource)
                .then(function (data) {
                    var teams = [];
                    angular.forEach(data, function (value, key) {
                        teams.push(new Team(value));
                    });
                    deferred.resolve(teams);
                }, function (error) {
                    deferred.reject(error);
                });

            return deferred.promise;
        }
    };
}]);