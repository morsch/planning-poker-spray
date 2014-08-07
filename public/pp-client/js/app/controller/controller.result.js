var result = angular.module('pp.controller.result', ['pp.service.estimates'], function () {
});

result.controller('ResultController', ['_', '$scope', '$rootScope', '$interval', '$stateParams', 'estimatesService',
    function (_, $scope, $rootScope, $interval, $stateParams, estimatesService) {
        $scope.ADDITIONAL_ROW_COUNT = 2;

         $scope.taskId = $stateParams.taskId;
	 $scope.toggleRow = function toggleRow(rowIndex) {
            //Block click on first row, already inactive rows and additional rows like average an median
            if (rowIndex == 0 || $scope.inactiveRows.indexOf(rowIndex) != -1 || rowIndex >= $scope.resultArray.length - $scope.ADDITIONAL_ROW_COUNT) {
                return;
            }
            var indexInArray = $scope.disabledRows.indexOf(rowIndex);
            if (indexInArray == -1) {
                $scope.disabledRows.push(rowIndex);
            } else {
                $scope.disabledRows.splice(indexInArray, 1);
            }
            update();
        };

        function ResultEntry(value, css) {
            this.value = value;
            this.css = css;
        };

        /*
         * Appends a result entry to specific result row.
         * if array or row does not exist it will be added
         */
        function addAppendResultEntryToIndex(array, i, value) {
            if (!array) {
                array = [];
            }

            if (!array[i]) {
                array[i] = []
            }

            array[i].push(value);
            return array;
        };


        var update = function () {
            estimatesService.getEstimatesByTask($stateParams.taskId).then(function (data) {
                    //Empty cell top left
                    var resultArray = addAppendResultEntryToIndex(undefined, 0, new ResultEntry('', '', false));
                    //Prepare inactive/disabled rows arrays
                    var inactiveRows = [];
                    var disabledRows = $scope.disabledRows;
                    if (!disabledRows) {
                        disabledRows = [];
                    }
                    //Unique usernames as list
                    var uniqueUsernames = _.uniq(data, function (estimate) {
                        return estimate.username;
                    });

                    //Unique types sorted in correct order
                    var sortedTypes = _.uniq(data, function (estimate) {
                        return estimate.type;
                    });

                    //Group by username
                    var groupedByUsername = _.groupBy(data, 'username');

                    //Group by username and type in correct order
                    var groupByUsernameAndType = [];
                    var activeRowCount = 0;
                    for (var i in uniqueUsernames) {
                        var name = uniqueUsernames[i].username;
                        var estimatesFromUser = groupedByUsername[name];
                        if (!groupByUsernameAndType[name]) {
                            groupByUsernameAndType[name] = [];
                        }
                        groupByUsernameAndType[name].activeRow = estimatesFromUser.length == sortedTypes.length;
                        if (groupByUsernameAndType[name].activeRow) {
                            activeRowCount++;
                        } else {
                            inactiveRows.push(parseInt(i) + 1);
                        }
                        for (var j in sortedTypes) {
                            var type = sortedTypes[j].type;
                            for (var k in estimatesFromUser) {
                                if (estimatesFromUser[k].type == type) {
                                    groupByUsernameAndType[name][type] = estimatesFromUser[k].amount;
                                }
                            }
                        }
                    }


                    //Add labels in first row
                    for (var i in sortedTypes) {
                        resultArray = addAppendResultEntryToIndex(resultArray, 0, new ResultEntry(sortedTypes[i].type, ''));
                    }
                    resultArray = addAppendResultEntryToIndex(resultArray, 0, new ResultEntry('Sum', 'sum'));


                    //Compute result array for estimates
                    var maxI = 0;
                    var minI = 0;
                    for (var i in uniqueUsernames) {
                        var nextIndex = parseInt(i) + 1;
                        var increased = false;
                        var name = uniqueUsernames[i].username;
                        var sum = 0;
                        resultArray = addAppendResultEntryToIndex(resultArray, nextIndex, new ResultEntry(name, ''));
                        for (var j in sortedTypes) {
                            var type = sortedTypes[j].type;
                            var amount = groupByUsernameAndType[name][type];
                            if (!amount) {
                                resultArray = addAppendResultEntryToIndex(resultArray, nextIndex, new ResultEntry('-', ''));
                            } else {
                                resultArray = addAppendResultEntryToIndex(resultArray, nextIndex, new ResultEntry(amount, ''));
                                sum += amount;
                            }
                        }
                        //Sum, Max/Min of sum

                        if (groupByUsernameAndType[name].activeRow) {
                            resultArray = addAppendResultEntryToIndex(resultArray, nextIndex, new ResultEntry(sum, 'sum'));
                            if (disabledRows.indexOf(nextIndex) == -1) {
                                if (maxI == 0 || resultArray[maxI][resultArray[maxI].length - 1].value < sum) {
                                    maxI = nextIndex;
                                }
                                if (minI == 0 || resultArray[minI][resultArray[minI].length - 1].value > sum) {
                                    minI = nextIndex;
                                }
                            }
                        } else {
                            resultArray = addAppendResultEntryToIndex(resultArray, nextIndex, new ResultEntry('-', 'sum'));
                        }
                    }
                    //Min/Max css for sum column only if it makes sense
                    var activeRowCount = (resultArray.length - 1) - (disabledRows.length + inactiveRows.length);
                    if (activeRowCount > 1) {
                        resultArray[maxI][resultArray[maxI].length - 1].css += ' max';
                        resultArray[minI][resultArray[minI].length - 1].css += ' min';
                    }
                    //Compute average and median and add to the end of result array
                    resultArray = addAppendResultEntryToIndex(resultArray, resultArray.length, new ResultEntry('Avg', 'sum'));
                    resultArray = addAppendResultEntryToIndex(resultArray, resultArray.length, new ResultEntry('Med', 'sum'));
                    //Length of rows are all equal, so we can iterate this way otherwise we would need to transpose
                    for (var i = 1; i < resultArray[0].length; i++) {
                        if (activeRowCount > 0) {
                            var medianArray = [];
                            var sum = 0;
                            for (var j = 1; j < resultArray.length; j++) {
                                var name = resultArray[j][0].value;
                                if (groupByUsernameAndType[name] && groupByUsernameAndType[name].activeRow && disabledRows.indexOf(j) == -1) {
                                    amount = resultArray[j][i].value;
                                    medianArray.push(amount);
                                    sum += amount;
                                }
                            }
                            if (medianArray.length > 0) {
                                resultArray = addAppendResultEntryToIndex(resultArray, resultArray.length - 2, new ResultEntry(Math.ceil(sum / activeRowCount), 'sum'));
                                var sortedMedian = _.sortBy(medianArray);
                                var median;
                                var medianIndex = Math.floor(sortedMedian.length / 2);
                                if (sortedMedian.length % 2 == 0) {
                                    median = Math.ceil((sortedMedian[medianIndex] + sortedMedian[medianIndex - 1]) / 2);
                                } else {
                                    median = sortedMedian[medianIndex];
                                }
                                resultArray = addAppendResultEntryToIndex(resultArray, resultArray.length - 1, new ResultEntry(median, 'sum'));
                            }
                        } else {
                            resultArray = addAppendResultEntryToIndex(resultArray, resultArray.length - 2, new ResultEntry('-', 'sum'));
                            resultArray = addAppendResultEntryToIndex(resultArray, resultArray.length - 1, new ResultEntry('-', 'sum'));
                        }
                    }
                    //Add to scope
                    $scope.resultArray = resultArray;
                    $scope.inactiveRows = inactiveRows;
                    $scope.disabledRows = disabledRows;
                }
            )
            ;
        };

        update();
        $rootScope.interval = $interval(function () {
            update()
        }, 5000);


    }])
;
