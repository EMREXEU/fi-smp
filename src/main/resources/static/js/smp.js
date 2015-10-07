app = angular.module('smp', ['ngRoute']);

app.config(function ($routeProvider, $httpProvider, $locationProvider) {

    $routeProvider.
            when('/', {
                templateUrl: 'partials/select_ncp.html',
                controller: 'home'
            }).
            otherwise({
                redirectTo: '/'
            });

    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

});

/*
 "countries": [
 { "countryName": "[Dev] Workshop", "countryCode": "WS", "singleFetch": true  },
 { "countryName": "[Dev] Finland",  "countryCode": "FI", "singleFetch": false },
 { "countryName": "[Dev] Sweden",   "countryCode": "SE", "singleFetch": false },
 { "countryName": "[Dev] Norway",   "countryCode": "NO", "singleFetch": false },
 { "countryName": "[Dev] Norway Alen",   "countryCode": "NOA", "singleFetch": false },
 { "countryName": "[Dev] Italy",    "countryCode": "IT", "singleFetch": true  }
 ]
 */


app.controller('home', function ($scope, $http) {

    var countryFlags =
        {Finland: 'finland.png',
         Denmark: 'denmark.png',
         Italy: 'italy.png',
         Norway: 'norway.png',
         Sweden: 'sweden.png'};

    $scope.ncpUrl = null;

    $scope.$watch('ncpUrl', function() {
        /*if ($scope.ncpUrl)
            angular.element('#form-button').trigger('click'); */
    });

    $scope.getFlag = function(countryName) {
        for (var country in countryFlags)
            if (countryName.indexOf(country) >= 0)
                return 'flags/' + countryFlags[country];
        return null;
    };

    $scope.selectCountry = function(country){
        var ncps = $scope.emreg.ncps.filter(function(ncp) {
            return ncp.countryCode == country.countryCode;
        });

        $scope.ncps = ncps;

        if (ncps.length == 1) {
            $scope.ncpUrl = ncps[0].url;
        }

    };


    $http.get('/smp/api/emreg').success(function (data) {
        $scope.emreg = data;
    })

});



