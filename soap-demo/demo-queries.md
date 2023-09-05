## Querying SOAP requests

## Get country info
```taxi
import com.orbitalhq.CurrencyName
import com.orbitalhq.CountryFlagUrl
import com.orbitalhq.CountryName

given { iso: IsoCountryCode = "NZ"}
find { 
    name: CountryName
    flag: CountryFlagUrl
    currency: CurrencyName
 }
```

### Saved query
```taxi
import com.orbitalhq.CapitalCityName
import com.orbitalhq.CountryFlagUrl
import com.orbitalhq.CountryName
import com.orbitalhq.IsoCountryCode
@HttpOperation(url = '/api/q/country-info-query/{countryCode}', method = 'GET')
query countries(@PathVariable("countryCode") countryCode: IsoCountryCode) {
   
   find { 
       name : CountryName
       flag: CountryFlagUrl
       capital: CapitalCityName
   }
}
```

### OpenApi spec
Available at:

`http://localhost:9022/api/q/meta/countryinfo/oas`
