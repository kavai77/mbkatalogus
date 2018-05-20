var app=angular.module('app', ['ngFileUpload', 'ui.bootstrap']);

app.controller('controller', function($scope, Upload, $interval, $http) {
    $scope.quality = "DRAFT";
    $scope.wholeSaleFormat = "true";
    $scope.autoLineBreakAfterMinQty = false;
    $scope.skipBoxSpaceOnBeginning = 0;
    $scope.send = function() {
        $scope.errorMessage = null;
        $scope.requestId = null;
        $scope.errorCount = 0;
        $scope.warningCount = 0;
        $scope.errorItems = new Array();
        if (!$scope.catalogueCsvFile) {
            $scope.errorMessage = "Válassz ki egy CSV fájlt!"
            return;
        }
        if (!$scope.catalogueTitle) {
            $scope.errorMessage = "Írj be egy katalógus címet!"
            return;
        }
        if ($scope.skipBoxSpaceOnBeginning > 15) {
            $scope.errorMessage = "Maximálisan 15 boxot hagyhatsz ki."
            return;
        }
        Upload.upload({
            url: '/service/csvRendering',
            data: {
                'file': $scope.catalogueCsvFile,
                'title': $scope.catalogueTitle,
                'quality': $scope.quality,
                'wholeSaleFormat': $scope.wholeSaleFormat,
                'autoLineBreakAfterMinQty': $scope.autoLineBreakAfterMinQty,
                'skipBoxSpaceOnBeginning': $scope.skipBoxSpaceOnBeginning,
            }
        }).then(function (resp) {
            $scope.requestId = resp.data.requestId;
            $scope.totalPageCount = 0;
            $scope.currentPageNumber = 0;
            stop = $interval(function() {
                $http.get('/service/pollUserInfo?requestId=' + $scope.requestId).then(
                function successCallback(response){
                    $scope.generatedDocuments = response.data.generatedDocuments;
                    $scope.totalPageCount = response.data.totalPageCount;
                    $scope.currentPageNumber = response.data.currentPageNumber;
                    $scope.appendErrorItems(response.data.errorItems);
                    $scope.done = response.data.done;
                    if ($scope.done) {
                        $interval.cancel(stop);
                    }
                }, function errorCallback(response) {
                    console.log(JSON.stringify(response));
                });
            }, 1000)
        }, function (resp) {
            $scope.errorMessage = "A feltöltés nem sikerült. Próbáld újra..."
        });
    }

    $scope.cancel = function() {
        $http.get('/service/cancel?requestId=' + $scope.requestId);
    }

    $http.get('/service/indexbootstrap').then(
        function successCallback(response){
            $scope.pageTitle = response.data.pageTitle;
        }, function errorCallback(response) {
            console.log(JSON.stringify(response));
        }
    );

    $scope.errorStyle = new Object();
    $scope.errorStyle["INFO"] = "alert alert-success";
    $scope.errorStyle["WARN"] = "alert alert-warning";
    $scope.errorStyle["ERROR"] = "alert alert-danger";
    $scope.hiddenErrorItems = new Object();

    $scope.appendErrorItems = function(errorItems) {
        for (i = 0; i < errorItems.length;i++) {
            switch (errorItems[i].severity) {
                case "WARN":
                    $scope.warningCount++;
                    break;
                case "ERROR":
                    $scope.errorCount++;
            }
            $scope.errorItems.push(errorItems[i]);
        }
    }
});
