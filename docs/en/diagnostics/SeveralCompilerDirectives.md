# Ошибочное указание нескольких директив компиляции

Указание нескольких директив компиляции методу или переменной модуля яявляется ошибкой

Неправильно

```Bsl
&НаСервере
&НаКлиенте
Перем МояПеременная;

&НаСервере
&НаКлиенте
Процедура МояПроцедура()

КонецПроцедуры
```