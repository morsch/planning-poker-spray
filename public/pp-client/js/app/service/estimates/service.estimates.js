var estimatesService = angular.module('pp.service.estimates', ['pp.service.rest'], function () {
});

estimatesService.constant('estimatesConstants', {
    resource: 'estimates'
});

estimatesService.factory('estimatesService', ['$q', 'restService', 'estimatesConstants', function ($q, restService, estimatesConstants) {

    function Estimate(username, type, amount) {
        this.username = username;
        this.type = type;
        this.amount = amount;
    };

    return {

        createEstimate: function (username, type, amount) {
            return new Estimate(username, type, amount);

        },

        getEstimatesByTask: function (taskId) {

            var deferred = $q.defer();

            restService.findAll(estimatesConstants.resource + '/' + taskId)
                .then(function (data) {
                    var estimates = [];
                    angular.forEach(data, function (value, key) {
                        estimates.push(new Estimate(value.username, value.type, value.amount));
                    });
                    deferred.resolve(estimates);
                }, function (error) {
                    deferred.reject(error);
                });

            return deferred.promise;
        },

        getEstimatesByTaskAndUser: function (taskId, userName) {

            var deferred = $q.defer();

            restService.findAll(estimatesConstants.resource + '/' + taskId + '/' + userName)
                .then(function (data) {
                    var estimates = [];
                    angular.forEach(data, function (value, key) {
                        estimates.push(new Estimate(value.username, value.type, value.amount));
                    });
                    deferred.resolve(estimates);
                }, function (error) {
                    deferred.reject(error);
                });

            return deferred.promise;
        },

        pushEstimate: function (taskId, userName, estimate) {

            var deferred = $q.defer();

            restService.post(estimatesConstants.resource + '/' + taskId + '/' + userName, estimate)
                .then(function (data) {
                    deferred.resolve(data);
                },
                function (error) {
                    deferred.reject(error);
                });

            return deferred.promise;
        }
    };
}]);